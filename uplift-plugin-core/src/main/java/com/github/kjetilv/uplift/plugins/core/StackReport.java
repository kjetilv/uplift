package com.github.kjetilv.uplift.plugins.core;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.Stack;
import software.amazon.awssdk.services.cloudformation.model.StackResource;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.FunctionUrlConfig;
import software.amazon.awssdk.services.lambda.model.ListFunctionUrlConfigsRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Reports what a deployed stack currently looks like: its functions, their function URLs,
 * and the local artifacts that produced them. Read only.
 */
public final class StackReport {

    private static final String FUNCTION = "AWS::Lambda::Function";

    /** CDK adds its own log retention functions. They are noise in this report. */
    private static final String LOG_RETENTION = "-LogRetention";

    private final String stack;

    private final String region;

    private final String profile;

    private final Log log;

    public StackReport(String stack, String region, String profile, Log log) {
        this.stack = stack;
        this.region = region;
        this.profile = profile;
        this.log = log;
    }

    public void report(List<Path> sources) {
        try (
            CloudFormationClient cloudFormation = cloudFormationClient();
            LambdaClient lambda = lambdaClient()
        ) {
            cloudFormation.describeStacks().stacks().stream()
                .filter(candidate -> candidate.stackName().equals(stack))
                .forEach(found -> report(cloudFormation, lambda, found, sources));
        }
    }

    private void report(
        CloudFormationClient cloudFormation,
        LambdaClient lambda,
        Stack found,
        List<Path> sources
    ) {
        List<StackResource> resources = cloudFormation.describeStackResources(request ->
            request.stackName(found.stackName())).stackResources();

        if (resources.isEmpty()) {
            log.warn(summary("No resources found for stack", found));
            return;
        }
        List<StackResource> functions = resources.stream().filter(StackReport::isFunction).toList();
        if (functions.isEmpty()) {
            log.warn(summary("No functions found for stack", found));
            resources.forEach(resource ->
                log.lifecycle("  " + resource.physicalResourceId() + ":" + resource.resourceType()));
            return;
        }
        log.lifecycle(
            """
            ##
            ## uplifted `%s`
            ##
            ##   Stack id  : %s
            ##   Created   : %s
            ##   Modified  : %s
            ##   Resources : %d
            ##
            ##  Lambdas:""".formatted(
                found.stackName(),
                found.stackId(),
                found.creationTime(),
                found.lastUpdatedTime(),
                resources.size()
            ));

        for (int index = 0; index < functions.size(); index++) {
            reportFunction(lambda, functions.get(index), index + 1);
        }
        reportSources(sources);
    }

    private void reportFunction(LambdaClient lambda, StackResource resource, int number) {
        configuration(lambda, resource).ifPresent(function ->
            urls(lambda, function).forEach(url ->
                log.lifecycle(
                    """
                    ##   [%d] %s: %s
                    ##    modified @ %s
                    ##    Function URL %s
                    ##      created @ %s
                    ##     modified @ %s
                    ##         cors : %s
                    ##         auth : %s""".formatted(
                        number,
                        function.functionName(),
                        description(function),
                        function.lastModified() == null ? "<unknown>" : function.lastModified(),
                        url.functionUrl(),
                        url.creationTime(),
                        url.lastModifiedTime(),
                        url.cors(),
                        url.authTypeAsString()
                    ))));
    }

    private void reportSources(List<Path> sources) {
        if (sources == null || sources.isEmpty()) {
            return;
        }
        log.lifecycle(
            """
            ##
            ##  Zips/binaries/jars:""");
        List<Path> all = new ArrayList<>();
        sources.forEach(zip -> {
            all.add(zip);
            binaryFor(zip).ifPresent(all::add);
            all.addAll(jarsBeside(zip));
        });
        all.sort(Comparator.comparing(StackReport::modified));

        for (int index = 0; index < all.size(); index++) {
            Path path = all.get(index);
            Instant time = modified(path).truncatedTo(ChronoUnit.SECONDS);
            log.lifecycle(
                """
                ##   [%d] %s %s → %s
                ##     full path : %s
                ##     real time : %s
                ##     size      : %s""".formatted(
                    index + 1,
                    local(time, DateTimeFormatter.ISO_LOCAL_TIME),
                    local(time, DateTimeFormatter.ISO_LOCAL_DATE),
                    path.getFileName(),
                    path.toAbsolutePath().toUri(),
                    time.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
                    size(path)
                ));
        }
    }

    private static String summary(String headline, Stack found) {
        return """
            ##
            ## %s `%s`
            ##   Stack id : %s
            ##   Created  : %s
            ##   Modified : %s
            ##""".formatted(
            headline,
            found.stackName(),
            found.stackId(),
            found.creationTime(),
            found.lastUpdatedTime()
        );
    }

    private static String description(FunctionConfiguration function) {
        String description = function.description();
        return description == null || description.isBlank() ? "" : description;
    }

    private static Optional<FunctionConfiguration> configuration(
        LambdaClient lambda,
        StackResource resource
    ) {
        return lambda.listFunctions().functions().stream()
            .filter(function -> function.functionName().equals(resource.physicalResourceId()))
            .findFirst();
    }

    private static List<FunctionUrlConfig> urls(LambdaClient lambda, FunctionConfiguration function) {
        return lambda.listFunctionUrlConfigs(
            ListFunctionUrlConfigsRequest.builder().functionName(function.functionName()).build()
        ).functionUrlConfigs();
    }

    private static boolean isFunction(StackResource resource) {
        return FUNCTION.equals(resource.resourceType())
               && !resource.physicalResourceId().contains(LOG_RETENTION);
    }

    private static Optional<Path> binaryFor(Path zip) {
        String name = zip.getFileName().toString();
        if (!name.endsWith(".zip") || zip.getParent() == null) {
            return Optional.empty();
        }
        return Optional.of(zip.getParent().resolve(name.substring(0, name.length() - ".zip".length())));
    }

    private static List<Path> jarsBeside(Path zip) {
        Path parent = zip.getParent();
        if (parent == null) {
            return List.of();
        }
        try (Stream<Path> found = Files.find(parent, 1, (file, _) ->
            file.getFileName().toString().endsWith(".jar"))) {
            return found.toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list jars beside " + zip, e);
        }
    }

    private static String local(Instant time, DateTimeFormatter format) {
        return time.atZone(ZoneId.systemDefault()).format(format);
    }

    private static Instant modified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read time of " + path, e);
        }
    }

    @SuppressWarnings("MagicNumber")
    private static String size(Path path) {
        long limit = 4;
        try {
            long size = Files.size(path);
            if (size > limit * 1_000_000) {
                return size / 1_000_000 + "Mb";
            }
            return size > limit * 1_000 ? size / 1_000 + "Kb" : size + "b";
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read size of " + path, e);
        }
    }

    private CloudFormationClient cloudFormationClient() {
        return CloudFormationClient.builder()
            .region(Region.of(region))
            .credentialsProvider(ProfileCredentialsProvider.create(profile))
            .build();
    }

    private LambdaClient lambdaClient() {
        return LambdaClient.builder()
            .region(Region.of(region))
            .credentialsProvider(ProfileCredentialsProvider.create(profile))
            .build();
    }
}

package com.github.kjetilv.uplift.plugins.core;

import module java.base;

import static com.github.kjetilv.uplift.plugins.core.Dist.Type.FILE;

/**
 * Builds a native lambda binary by rendering a Dockerfile, building the image, running it
 * to produce the binary, and zipping the result.
 */
public final class NativeImage {

    private final Spec spec;

    private final Path upliftDir;

    private final Path classpathDir;

    private final Docker docker;

    private final Log log;

    public NativeImage(Spec spec, Path upliftDir, Path classpathDir, Docker docker, Log log) {
        this.spec = spec;
        this.upliftDir = upliftDir;
        this.classpathDir = classpathDir;
        this.docker = docker;
        this.log = log;
    }

    /**
     * @param emptyClasspathHint Named in the warning when nothing resolved, so the caller
     *                           can say which upstream tasks or modules were consulted
     */
    public Path build(String emptyClasspathHint) {
        Dist dist = Dist.verified(spec.javaDist());
        List<Path> classPath = spec.classPath();

        if (classPath.isEmpty()) {
            log.warn("No output jars from any upstream dependencies: " + emptyClasspathHint);
        }
        classPath.forEach(entry -> FileIO.copyTo(entry, classpathDir));

        List<String> dockerfile = Templates.renderResource(TEMPLATE, parameters(dist, classPath));
        try {
            Files.write(upliftDir.resolve("Dockerfile"), dockerfile);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write Dockerfile in " + upliftDir, e);
        }
        log.info("Created new DockerFile for " + spec.buildsite());

        if (dist.type() == FILE) {
            FileIO.copyTo(dist.path(), upliftDir, "dist.tar.gz");
        }

        docker.run(upliftDir, "build --tag " + spec.buildsite() + ":latest " + upliftDir);
        docker.run(upliftDir, "build --tag " + spec.identifier() + ":latest " + upliftDir);
        docker.run(
            upliftDir,
            "run -v " + upliftDir.toAbsolutePath() + ":/out " + spec.buildsite() + ":latest"
        );

        return Zips.zip(upliftDir.resolve(spec.identifier()), spec.zipFile());
    }

    private Map<String, String> parameters(Dist dist, List<Path> classPath) {
        var distLines = switch (dist.type()) {
            case FILE -> Templates.renderResource(
                TEMPLATE_DISTFILE,
                Map.of("distfile", dist.uriString())
            );
            case HTTP -> Templates.renderResource(
                TEMPLATE_DISTURI,
                Map.of("disturi", dist.uriString())
            );
        };
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("buildsite", spec.buildsite());
        parameters.put("target", spec.identifier());
        parameters.put("arch", spec.arch());
        parameters.put("main", spec.main());
        parameters.put("enablepreview", spec.enablePreview() ? "--enable-preview" : "");
        parameters.put("addmodules", option("--add-modules ", spec.addModules()));
        parameters.put("otheroptions", option("", spec.otherOptions()));
        parameters.put("dist", String.join("\n", distLines));
        parameters.put("classpath", contained(classPath));
        return parameters;
    }

    private static final String TEMPLATE = "lambda/Dockerfile";

    private static final String TEMPLATE_DISTFILE = "lambda/Dockerfile-distfile";

    private static final String TEMPLATE_DISTURI = "lambda/Dockerfile-disturi";

    /** Where the classpath lands inside the container, per the volume mount below. */
    private static final String CONTAINED_CLASSPATH = "/out/classpath/";

    private static String option(String prefix, String value) {
        return value != null && !value.isBlank() ? prefix + value : "";
    }

    private static String contained(List<Path> classPath) {
        return classPath.stream()
            .map(entry -> CONTAINED_CLASSPATH + entry.getFileName())
            .collect(Collectors.joining(":"));
    }

    public record Spec(
        String identifier,
        String arch,
        String main,
        URI javaDist,
        String buildsite,
        Path zipFile,
        List<Path> classPath,
        boolean enablePreview,
        String addModules,
        String otherOptions
    ) {
    }
}

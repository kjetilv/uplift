package com.github.kjetilv.uplift.plugins.maven;

import com.github.kjetilv.uplift.plugins.core.Docker;
import com.github.kjetilv.uplift.plugins.core.NativeImage;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.StringUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a GraalVM native image in a container and attaches the result as a zip artifact.
 * <p>
 * The zip is attached rather than left on disk, because the deploy goal runs in a different
 * module and Maven has no way to reach into another module's build directory.
 */
@Mojo(
    name = "native-lambda",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    threadSafe = true
)
public class NativeLambdaMojo extends AbstractMojo {

    private static final String GRAAL_DIST =
        "https://github.com/graalvm/graalvm-ce-builds/releases/download/graal-25.2.4/"
        + "graalvm-community-jdk-25i2-25.0.4_linux-aarch64_bin.tar.gz";

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /** Entry point passed to native-image. */
    @Parameter(property = "uplift.main", required = true)
    private String main;

    @Parameter(property = "uplift.identifier", defaultValue = "${project.artifactId}")
    private String identifier;

    /**
     * Raw {@code os.arch}, which is what the lambda Dockerfile wants. Note this differs
     * from the CDK goals, which translate it to docker's own naming.
     */
    @Parameter(property = "uplift.arch")
    private String arch;

    @Parameter(property = "uplift.buildsite")
    private String buildsite;

    @Parameter(property = "uplift.javaDist", defaultValue = GRAAL_DIST)
    private String javaDist;

    @Parameter(property = "uplift.enablePreview", defaultValue = "false")
    private boolean enablePreview;

    @Parameter(property = "uplift.addModules", defaultValue = "")
    private String addModules;

    @Parameter(property = "uplift.otherOptions", defaultValue = "")
    private String otherOptions;

    @Parameter(property = "docker.binary", defaultValue = Docker.DEFAULT_BINARY)
    private String dockerBinary;

    /**
     * Skips the build. The goal needs a running docker daemon and takes minutes, so CI
     * that only wants the libraries compiled and tested can leave it out.
     */
    @Parameter(property = "uplift.skipNativeLambda", defaultValue = "false")
    private boolean skip;

    private final MavenProjectHelper projectHelper;

    @Inject
    public NativeLambdaMojo(MavenProjectHelper projectHelper) {
        this.projectHelper = projectHelper;
    }

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Skipping native lambda build, uplift.skipNativeLambda is set");
            return;
        }
        Path uplift = buildSubDirectory("uplift");
        Path zipFile = uplift.resolve(identifier + ".zip");

        NativeImage.Spec spec = new NativeImage.Spec(
            identifier,
            arch != null && !arch.isBlank() ? arch : System.getProperty("os.arch"),
            main,
            URI.create(javaDist),
            buildsite != null && !buildsite.isBlank() ? buildsite : shortGroupName() + "-buildsite",
            zipFile,
            classPath(),
            enablePreview,
            addModules,
            otherOptions
        );
        try {
            Path built = new NativeImage(
                spec,
                uplift,
                buildSubDirectory("uplift/classpath"),
                new Docker(dockerBinary, new MavenLog(getLog())),
                new MavenLog(getLog())
            ).build("dependencies of " + project.getArtifactId());

            projectHelper.attachArtifact(project, "zip", built.toFile());
            getLog().info("Attached " + built.getFileName() + " as a zip artifact");
        } catch (RuntimeException e) {
            throw new MojoExecutionException("Failed to build native lambda " + identifier, e);
        }
    }

    /**
     * Dependency jars in resolution order, then this module's own jar.
     * <p>
     * {@code getRuntimeClasspathElements} puts {@code target/classes} first, which is the
     * module's own compiled output. That is dropped in favour of the packaged jar appended
     * at the end, so the container sees one jar per entry and the module's own code last,
     * matching what the Gradle task produced.
     */
    private List<Path> classPath() throws MojoExecutionException {
        List<Path> classPath = new ArrayList<>();
        String ownClasses = project.getBuild().getOutputDirectory();
        try {
            for (String element : project.getRuntimeClasspathElements()) {
                if (!element.equals(ownClasses)) {
                    classPath.add(Path.of(element));
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to resolve the runtime classpath", e);
        }
        File own = project.getArtifact() == null ? null : project.getArtifact().getFile();
        if (own == null) {
            throw new MojoExecutionException(
                "No jar built for " + project.getArtifactId()
                + ". This goal must run after package, not before.");
        }
        classPath.add(own.toPath());
        return classPath;
    }

    private String shortGroupName() {
        String group = project.getGroupId();
        int dot = group.lastIndexOf('.');
        return dot > 0 ? group.substring(dot + 1) : group;
    }

    private Path buildSubDirectory(String name) {
        Path path = Path.of(project.getBuild().getDirectory()).resolve(name);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create " + path, e);
        }
        return path;
    }
}

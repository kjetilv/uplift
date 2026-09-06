package com.github.kjetilv.uplift.plugins.maven;

import com.github.kjetilv.uplift.plugins.core.FileIO;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/** Base for the goals that need the lambda zips staged where the container can see them. */
public abstract class AbstractLambdaZipMojo extends AbstractUpliftMojo {

    /** Overrides discovery. Normally the zips come from dependencies of type zip. */
    @Parameter(property = "uplift.lambdaZips")
    protected List<File> lambdaZips;

    /**
     * Stages the zips under the directory mounted at {@code /lambdas}.
     * <p>
     * The name matters. Stack builders refer to assets by a fixed path, for example
     * {@code /lambdas/kudu.zip}, but Maven resolves an artifact to
     * {@code kudu-0.1.1-SNAPSHOT.zip}. Each zip is therefore copied under its artifact id,
     * dropping the version.
     */
    protected final void collectLambdaZips() throws MojoExecutionException {
        if (lambdaZips != null && !lambdaZips.isEmpty()) {
            lambdaZips.forEach(zip -> FileIO.copyTo(zip.toPath(), upliftDir()));
            return;
        }
        List<Artifact> zips = project.getArtifacts().stream()
            .filter(artifact -> "zip".equals(artifact.getType()))
            .filter(artifact -> artifact.getFile() != null)
            .toList();
        if (zips.isEmpty()) {
            throw new MojoExecutionException(
                "No zips configured, and no dependencies of type zip. Declare the lambda "
                + "modules with <type>zip</type>, or set uplift.lambdaZips.");
        }
        zips.forEach(artifact ->
            FileIO.copyTo(artifact.getFile().toPath(), upliftDir(), artifact.getArtifactId() + ".zip"));
    }

    /** The staged zips, by the names they were given above. */
    protected final List<Path> stagedZips() {
        if (lambdaZips != null && !lambdaZips.isEmpty()) {
            return lambdaZips.stream().map(zip -> upliftDir().resolve(zip.toPath().getFileName())).toList();
        }
        return project.getArtifacts().stream()
            .filter(artifact -> "zip".equals(artifact.getType()))
            .map(artifact -> upliftDir().resolve(artifact.getArtifactId() + ".zip"))
            .toList();
    }
}

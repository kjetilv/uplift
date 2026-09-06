package com.github.kjetilv.uplift.plugins;

import com.github.kjetilv.uplift.plugins.core.Cdk;
import com.github.kjetilv.uplift.plugins.core.Docker;
import com.github.kjetilv.uplift.plugins.core.StackReport;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base for the tasks that talk to AWS through the CDK container.
 * <p>
 * Coordinates are {@code Property} values, set either from project properties by
 * {@link UpliftPlugin} or directly in a build script, for example
 * {@code tasks.withType<UpliftTask> { stack.set("taninim") } }.
 */
@CacheableTask
public abstract class UpliftTask extends DefaultTask {

    public UpliftTask() {
        setGroup("uplift");
    }

    @Input
    public abstract Property<String> getArch();

    @Input
    public abstract MapProperty<String, String> getEnv();

    @Input
    public abstract Property<String> getAwsAuth();

    @Input
    public abstract Property<String> getAccount();

    @Input
    public abstract Property<String> getProfile();

    @Input
    public abstract Property<String> getRegion();

    @Input
    public abstract Property<String> getStack();

    @TaskAction
    public final void upliftPerform() {
        selfCheck();
        cdk().initialize();
        perform();
    }

    /**
     * Names the class that builds the stack. A no-op unless this task generates the CDK
     * app, so that a build script can apply it across all uplift tasks at once.
     */
    public void stackWith(String name) {
    }

    protected abstract void perform();

    protected final Cdk cdk() {
        return new Cdk(
            upliftDir(),
            cdkApp(),
            getAwsAuth().get(),
            getArch().get(),
            getEnv().getOrElse(Map.of()),
            docker(),
            log()
        );
    }

    protected final Path upliftDir() {
        return Projects.buildSubDirectory(getProject(), "uplift");
    }

    protected final Path cdkApp() {
        return Projects.cdkApp(getProject());
    }

    protected final String profileOption() {
        String profile = getProfile().getOrNull();
        return profile == null || profile.isBlank() ? "" : "--profile=" + profile;
    }

    protected final void report(List<Path> sources) {
        new StackReport(getStack().get(), getRegion().get(), getProfile().get(), log()).report(sources);
    }

    protected final GradleLog log() {
        return new GradleLog(getLogger());
    }

    private Docker docker() {
        return new Docker(
            Projects.resolveProperty(getProject(), Docker.BINARY_PROPERTY, null, Docker.DEFAULT_BINARY),
            log()
        );
    }

    private void selfCheck() {
        List<String> missing = new ArrayList<>();
        checkPresent("account", getAccount(), missing);
        checkPresent("region", getRegion(), missing);
        checkPresent("profile", getProfile(), missing);
        checkPresent("stack", getStack(), missing);
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                "Missing config! Either set these on the task, or provide gradle.properties:\n "
                + String.join("\n ", missing));
        }
    }

    private static void checkPresent(String name, Property<String> property, List<String> missing) {
        String value = property.getOrNull();
        if (value == null || value.isBlank()) {
            missing.add(name);
        }
    }
}

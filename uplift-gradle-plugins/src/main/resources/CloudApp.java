package lambda.uplift.app;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.JarFile;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public final class CloudApp {

    public static void main(String[] args) {
        App app = new App();
        try {
            Stack stack = new Stack(
                app,
                STACK,
                StackProps.builder().env(
                        Environment.builder()
                            .account(ACCOUNT)
                            .region(REGION)
                            .build())
                    .build()
            );
            resolveStackloaderClass()
                .map(CloudApp::newInstance)
                .ifPresentOrElse(
                    consumer ->
                        consumer.accept(stack),
                    () -> {
                        throw new IllegalStateException("No stack loader in " + JAR);
                    }
                );
        } finally {
            app.synth();
        }
    }

    private static final String ACCOUNT =
        Optional.ofNullable(System.getProperty("uplift.account")).orElseThrow(() ->
            new IllegalStateException("Missing account"));

    private static final String REGION =
        Optional.ofNullable(System.getProperty("uplift.region")).orElseThrow(() ->
            new IllegalStateException("Missing region"));

    private static final String STACK =
        Optional.ofNullable(System.getProperty("uplift.stack")).orElseThrow(() ->
            new IllegalStateException("Missing stack"));

    private static final String JAR =
        Optional.ofNullable(System.getProperty("uplift.stackbuilderJar")).orElseThrow(() ->
            new IllegalStateException("Jar file missing"));

    private static final String BUILDER =
        Optional.ofNullable(System.getProperty("uplift.stackbuilderClass")).orElse("");

    private static <U> U newInstance(Class<U> consumerClass) {
        try {
            return consumerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Could not create instance of " + consumerClass, e);
        }
    }

    private static Optional<Class<Consumer<? super Stack>>> resolveStackloaderClass() {
        try (
            JarFile jarFile = new JarFile(JAR)
        ) {
            return jarFile.stream()
                .filter(entry ->
                    entry.getName().endsWith(".class"))
                .map(entry ->
                    className(entry.getName()))
                .filter(name ->
                    BUILDER.isBlank() || BUILDER.equals(name))
                .map(CloudApp::loadClass)
                .map(CloudApp::asSupplier)
                .findFirst();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve stack loader class of " + JAR, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<Consumer<? super Stack>> asSupplier(Class<?> clazz) {
        if (Consumer.class.isAssignableFrom(clazz)) {
            // TODO Check generic type!
            return (Class<Consumer<? super Stack>>) clazz;
        }
        throw new IllegalStateException(
            "Not a " + Consumer.class.getSimpleName() + "<" + Stack.class.getSimpleName() + ">: " + clazz);
    }

    private static Class<?> loadClass(String name) {
        try (
            URLClassLoader classLoader = getUrlClassLoader()
        ) {
            return classLoader.loadClass(name);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load: " + name, e);
        }
    }

    private static URLClassLoader getUrlClassLoader() {
        return new URLClassLoader(
            new URL[] { stackloaderURL() },
            Thread.currentThread().getContextClassLoader()
        );
    }

    private static String className(String name) {
        return name.substring(0, name.indexOf(".class")).replace('/', '.');
    }

    private static URL stackloaderURL() {
        try {
            return Path.of(JAR).toUri().toURL();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load " + JAR, e);
        }
    }
}

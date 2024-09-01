package com.github.kjetilv.uplift.cdk;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.lambda.*;

import java.util.Objects;

import static software.amazon.awscdk.services.lambda.Runtime.PROVIDED_AL2023;

record DefStacker(
    String name,
    String module,
    boolean url,
    Settings settings,
    boolean withCors,
    Cors cors
) implements Stacker {

    DefStacker(
        String name,
        String module,
        boolean url,
        Settings settings,
        boolean withCors,
        Cors cors
    ) {
        this.name = name == null || name.isBlank() ? module + "-lambda" : module;
        this.module = Objects.requireNonNull(module, "module");
        this.url = url;
        this.settings = settings == null ? new Settings() : settings;
        this.withCors = withCors;
        this.cors = cors == null ? new Cors(): cors;
    }

    DefStacker(String name, String module) {
        this(name,
            Objects.requireNonNull(module, "module"),
            false,
            null,
            false,
            null);
    }

    @Override
    public Stacker withUrl() {
        return new DefStacker(name,
            module,
            true,
            settings,
            withCors,
            cors);
    }

    @Override
    public Stacker cors(java.util.function.Function<Cors, Cors> cors) {
        return new DefStacker(name,
            module,
            true,
            settings,
            true,
            Objects.requireNonNull(cors, "cors").apply(this.cors));
    }

    @Override
    public Stacker settings(java.util.function.Function<Settings, Settings> settings) {
        return new DefStacker(name,
            module,
            url,
            Objects.requireNonNull(settings).apply(this.settings),
            withCors,
            cors);

    }

    @Override
    public void accept(Stack stack) {
        FunctionUrl.Builder.create(stack, name + "-fun-url")
            .function(Function.Builder.create(stack, name + "-fun-id")
                .functionName(name)
                .code(Code.fromAsset("/lambdas/" + module + ".zip"))
                .handler("bootstrap")
                .logRetention(this.settings().logRetention())
                .architecture(settings.architecture())
                .memorySize(settings.memoryMb())
                .runtime(PROVIDED_AL2023)
                .timeout(software.amazon.awscdk.Duration.seconds(this.settings().timeout().getSeconds()))
                .build())
            .authType(FunctionUrlAuthType.NONE)
            .cors(FunctionUrlCorsOptions.builder()
                .allowedMethods(cors.methods())
                .allowedOrigins(cors.origins())
                .allowedHeaders(cors.headers())
                .maxAge(software.amazon.awscdk.Duration.seconds(cors.maxAge().getSeconds()))
                .allowCredentials(cors.allowCredentials())
                .build())
            .build();
    }
}

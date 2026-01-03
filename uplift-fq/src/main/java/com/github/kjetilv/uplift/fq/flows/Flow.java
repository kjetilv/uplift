package com.github.kjetilv.uplift.fq.flows;

public record Flow<T>(
    Name from,
    Name to,
    FqFlows.Processor<T> processor
) {

    public Name name() {
        return to();
    }

    public String description() {
        return from != null
            ? String.format("%s->%s/%s", from.name(), to.name(), procString())
            : String.format("|->%s/%s", to.name(), procString());
    }

    public Flow<T> from(Name from) {
        return new Flow<>(from, to, processor);
    }

    boolean isFromSource() {
        return from == null;
    }

    private String procString() {
        var procString = processor.toString();
        var lambdaIndex = procString.indexOf("$$Lambda/");
        if (lambdaIndex < 0) {
            return  procString;
        }
        var lambdaPrefix = procString.substring(0, lambdaIndex);
        var dotIndex = lambdaPrefix.lastIndexOf(".");
        return  lambdaPrefix.substring(dotIndex + 1) + "𝛌";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + description() + "]";
    }
}

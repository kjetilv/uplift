package com.github.kjetilv.uplift.fq.flows;

import module java.base;

final class Flows {

    static <T> void validateAll(List<Flow<T>> flows) {
        requireFlows(flows);
        var sources = sourceFlows(flows);
        if (sources.isEmpty()) {
            throw new IllegalStateException("No source intake defined: " + print(flows));
        }
        validate(flows);
    }

    static <T> void validate(List<Flow<T>> flows) {
        requireFlows(flows);
        var dupes = duplicate(flows);
        if (!dupes.isEmpty()) {
            throw new IllegalStateException(
                "Duplicate flow descriptions: " + String.join(", ", dupes.keySet())
            );
        }
        var missingInputs = dangling(flows);
        if (!missingInputs.isEmpty()) {
            throw new IllegalStateException("Missing inputs to flows: " + print(missingInputs));
        }
    }

    private static <T> void requireFlows(List<Flow<T>> flows) {
        if (flows == null || flows.isEmpty()) {
            throw new IllegalStateException("No flows defined");
        }
    }

    private static <T> Map<String, List<Flow<T>>> duplicate(List<Flow<T>> flows) {
        var groups = flows.stream()
            .collect(Collectors.groupingBy(Flow::description));
        return groups.entrySet()
            .stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(
                Collectors.toUnmodifiableMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                ));
    }

    private static <T> List<Flow<T>> dangling(List<Flow<T>> flows) {
        var downstream = flows.stream()
            .filter(flow ->
                !flow.isFromSource())
            .toList();
        var tos = flows.stream()
            .collect(Collectors.groupingBy(Flow::to));
        return downstream.stream()
            .filter(flow -> !tos.containsKey(flow.to()))
            .toList();
    }

    private static <T> List<Flow<T>> sourceFlows(List<Flow<T>> flows) {
        return flows.stream()
            .filter(Flow::isFromSource)
            .toList();
    }

    private Flows() {
    }

    private static <T> String print(List<Flow<T>> flows) {
        return flows.stream()
            .map(Flow::description)
            .collect(Collectors.joining(" "));
    }
}

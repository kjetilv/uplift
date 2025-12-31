package com.github.kjetilv.uplift.fq.flows;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class Flows {

    static <T> void validateAll(List<Flow<T>> flows) {
        validateIntakes(flows);
        validate(flows);
    }

    static <T> void validate(List<Flow<T>> flows) {
        var groups = flows.stream()
            .collect(Collectors.groupingBy(Flow::description));
        var dupes = groups.entrySet()
            .stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                ));
        if (!dupes.isEmpty()) {
            throw new IllegalStateException(
                "Duplicate flow descriptions: " + String.join(", ", dupes.keySet())
            );
        }
        var downstream = flows.stream()
            .filter(flow ->
                !flow.isFromSource())
            .toList();
        var tos = flows.stream()
            .collect(Collectors.groupingBy(Flow::to));
        var missingInputs = downstream.stream()
            .filter(flow -> !tos.containsKey(flow.to()))
            .toList();
        if (!missingInputs.isEmpty()) {
            throw new IllegalStateException("Missing inputs to flows: " + print(missingInputs));
        }
    }

    static <T> void validateIntakes(List<Flow<T>> flows) {
        var sources = flows.stream()
            .filter(Flow::isFromSource)
            .toList();
        if (sources.isEmpty()) {
            throw new IllegalStateException("No source intake defined: " + print(flows));
        }
    }

    private Flows() {
    }

    private static <T> String print(List<Flow<T>> flows) {
        return flows.stream()
            .map(Flow::description)
            .collect(Collectors.joining(" "));
    }
}

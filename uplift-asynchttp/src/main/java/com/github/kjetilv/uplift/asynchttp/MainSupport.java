package com.github.kjetilv.uplift.asynchttp;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public final class MainSupport {

    public static boolean boolArg(Map<String, String> map, String param) {
        return Optional.ofNullable(map.get(param))
            .or(() -> caseInsensitiveLookup(param, map))
            .map(Boolean::valueOf)
            .orElse(false);
    }

    public static int intArg(String[] args, String param, int defaultValue) {
        return intArg(parameterMap(args), param, defaultValue);
    }

    public static int intArg(Map<String, String> map, String param, int defaultValue) {
        return possibleIntArg(map, param)
            .orElse(defaultValue);
    }

    public static Optional<Integer> possibleIntArg(Map<String, String> map, String param) {
        return Optional.ofNullable(map.get(param))
            .or(() -> caseInsensitiveLookup(param, map))
            .map(Long::parseLong)
            .map(Math::toIntExact);
    }

    public static int validatePort(int port) {
        if (port > PORTS_AVAILABLE) {
            throw new IllegalStateException("Invalid port: " + port);
        }
        return port > 0 ? port : PORT_80;
    }

    public static Map<String, String> parameterMap(String[] args) {
        return toMap(args, '=', String::toLowerCase);
    }

    private MainSupport() {
    }

    public static final int MAX_REQUEST_SIZE = 4096;

    private static final int PORT_80 = 80;

    private static final int PORTS_AVAILABLE = 65535;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<Map.Entry<String, String>> EMPTY = Optional.empty();

    private static Optional<String> caseInsensitiveLookup(String param, Map<String, String> map) {
        return map.entrySet()
            .stream()
            .filter(e ->
                e.getKey()
                    .equalsIgnoreCase(param))
            .findFirst()
            .map(Map.Entry::getValue);
    }

    @SuppressWarnings("SameParameterValue")
    private static Map<String, String> toMap(
        String[] args, char split, Function<? super String, String> keyTrans
    ) {
        return mapOut(Arrays.stream(args), split, keyTrans);
    }

    private static Map<String, String> mapOut(
        Stream<String> stream, char split, Function<? super String, String> keyTrans
    ) {
        Function<? super String, String> parser = keyTrans == null ? Function.identity() : keyTrans;
        return stream.map(arg -> entry(split, parser, arg))
            .flatMap(Optional::stream)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Optional<Map.Entry<String, String>> entry(
        char splitter,
        Function<? super String, String> parser,
        String arg
    ) {
        int idx = arg.indexOf(splitter);
        return idx > 0
            ? entry(arg, idx, parser)
            : Optional.of(Map.entry(arg, "true"));
    }

    private static Optional<Map.Entry<String, String>> entry(
        String entry, int index, Function<? super String, String> parser
    ) {
        return Optional.of(entry.substring(0, index))
            .filter(s -> !s.isBlank())
            .map(String::trim)
            .map(key ->
                Map.entry(
                    parser.apply(key),
                    value(entry, index)
                ));
    }

    private static String value(String entry, int index) {
        int valueIndex = index + 1;
        return valueIndex > entry.length()
            ? ""
            : entry.substring(valueIndex).trim();
    }
}

package com.github.kjetilv.uplift.synchttp.req;

import com.github.kjetilv.uplift.synchttp.util.Utils;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public record QueryParameters(int startIndex, QueryParameter[] parameters) {

    public static final QueryParameter[] NONE = new QueryParameter[0];

    static QueryParameters parse(MemorySegment segment, int offset, int length) {
        var paramStart = Utils.indexOf((byte) '?', segment, offset, length);
        if (paramStart < 0) {
            return new QueryParameters(offset, NONE);
        }

        var offsets = new Offsets(
            segment,
            paramStart + 1,
            length,
            (byte) '&',
            (byte) '='
        );

        var state = new QueryParamCallbacks(segment, offset, length, paramStart + 1);
        offsets.scan(state);
        var queryParameters = state.finish();
        return queryParameters.length == 0
            ? new QueryParameters(paramStart, NONE)
            : new QueryParameters(paramStart, queryParameters);
    }

    public List<String> pars(String name) {
        return Arrays.stream(parameters)
            .filter(parameter ->
                parameter.hasName(name))
            .map(QueryParameter::value)
            .toList();
    }

    public String par(String name) {
        for (QueryParameter parameter : parameters) {
            if (parameter.hasName(name)) {
                return parameter.value();
            }
        }
        return null;
    }

    public Map<String, Object> toMap() {
        return Arrays.stream(parameters)
            .map(parameter ->
                Map.entry(parameter.name(), parameter.value()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
    }

    String value(String name) {
        for (QueryParameter parameter : parameters) {
            if (parameter.hasName(name)) {
                return parameter.value();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "&" + Arrays.stream(parameters)
            .map(Object::toString)
            .collect(Collectors.joining("&"));
    }
}

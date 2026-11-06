package com.github.kjetilv.uplift.synchttp.rere;

import module java.base;
import com.github.kjetilv.uplift.synchttp.Utils;

@SuppressWarnings("unused")
public record QueryParameters(int startIndex, QueryParameter... parameters) {

    public static final QueryParameter[] NONE = new QueryParameter[0];

    /// Parses the query part of a URL. Parameters are separated by `&`. A parameter without `=` has an empty
    /// value, and a value may contain `=`. Empty parameters are skipped.
    ///
    /// @param segment Segment
    /// @param offset  Start of the URL
    /// @param length  Length of the URL
    /// @return Parameters. The start index is the offset of `?`, or the end of the URL if there is no query.
    static QueryParameters parse(MemorySegment segment, int offset, int length) {
        var end = offset + length;
        var questionMark = Utils.indexOf((byte) '?', segment, offset, length);
        if (questionMark < 0) {
            return new QueryParameters(end, NONE);
        }
        List<QueryParameter> parameters = new ArrayList<>();
        var start = questionMark + 1;
        while (start <= end) {
            var ampersand = Utils.indexOf((byte) '&', segment, start, end - start);
            var parameterEnd = ampersand < 0 ? end : ampersand;
            if (parameterEnd > start) {
                var equals = Utils.indexOf((byte) '=', segment, start, parameterEnd - start);
                parameters.add(equals < 0
                    ? new QueryParameter(segment, start, parameterEnd, 0)
                    : new QueryParameter(segment, start, equals, parameterEnd - equals - 1));
            }
            start = parameterEnd + 1;
        }
        return new QueryParameters(
            questionMark,
            parameters.isEmpty() ? NONE : parameters.toArray(QueryParameter[]::new)
        );
    }

    public boolean isEmpty() {
        return parameters.length == 0;
    }

    public List<String> pars(String name) {
        return Arrays.stream(parameters)
            .filter(parameter ->
                parameter.hasName(name))
            .map(QueryParameter::value)
            .toList();
    }

    public String par(String name) {
        for (var parameter : parameters) {
            if (parameter.hasName(name)) {
                return parameter.value();
            }
        }
        return null;
    }

    /// @return Parameters by name. Values of parameters with the same name are joined with `,`.
    public Map<String, Object> toMap() {
        return Arrays.stream(parameters)
            .collect(Collectors.toMap(
                QueryParameter::name,
                QueryParameter::value,
                (value1, value2) -> value1 + "," + value2,
                LinkedHashMap::new
            ));
    }

    String value(String name) {
        for (var parameter : parameters) {
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

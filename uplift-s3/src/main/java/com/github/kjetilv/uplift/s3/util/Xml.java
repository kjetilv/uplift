package com.github.kjetilv.uplift.s3.util;

import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Xml {

    @SuppressWarnings("SameParameterValue")
    public static Stream<String> objectList(String xml, String field) {
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<>(
            Long.MAX_VALUE,
            Spliterator.ORDERED
        ) {

            private final String start = "<" + field + ">";

            private final String end = "</" + field + ">";

            private int index;

            @Override
            public boolean tryAdvance(Consumer<? super String> action) {
                int nextStart = xml.indexOf(start, index);
                if (nextStart < 0) {
                    return false;
                }
                int startPosition = nextStart + start.length();
                int endPosition = xml.indexOf(end, startPosition + start.length());
                if (endPosition < 0) {
                    throw new IllegalStateException("No closing tag for " + start + " after index " + startPosition);
                }
                action.accept(xml.substring(startPosition, endPosition));
                index = endPosition + end.length();
                return true;
            }
        }, false);
    }

    public static Stream<Map.Entry<String, String>> objectFields(String xml, String... fields) {
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<>(
            fields.length,
            Spliterator.ORDERED
        ) {

            private int index;

            private int position;

            @Override
            public boolean tryAdvance(Consumer<? super Map.Entry<String, String>> action) {
                String field = fields[index];
                String start = "<" + field + ">";
                String end = "</" + field + ">";
                int startPosition = xml.indexOf(start, position) + start.length();
                if (startPosition < 0) {
                    return false;
                }
                int endPosition = xml.indexOf(end, position);
                if (endPosition < 0) {
                    return false;
                }
                action.accept(Map.entry(field, xml.substring(startPosition, endPosition).trim()));
                index++;
                position = endPosition + end.length();
                return index < fields.length;
            }
        }, false);
    }

    private Xml() {

    }
}

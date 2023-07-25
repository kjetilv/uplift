package com.github.kjetilv.uplift.s3.util;

import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

final class EntrySpliterator extends Spliterators.AbstractSpliterator<Map.Entry<String, String>> {

    private final String xml;

    private final String[] fields;

    private int index;

    private int position;

    EntrySpliterator(String xml, String... fields) {
        super(fields.length, Spliterator.ORDERED);
        this.xml = xml;
        this.fields = fields;
    }

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
}

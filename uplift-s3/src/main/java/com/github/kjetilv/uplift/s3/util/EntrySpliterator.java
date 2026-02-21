package com.github.kjetilv.uplift.s3.util;

import module java.base;

final class EntrySpliterator extends Spliterators.AbstractSpliterator<Map.Entry<String, String>> {

    private final String xml;

    private final String[] fields;

    private int index;

    private int position;

    EntrySpliterator(String xml, String... fields) {
        super(fields.length, ORDERED);
        this.xml = xml;
        this.fields = fields;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Map.Entry<String, String>> action) {
        var field = fields[index];
        var start = "<" + field + ">";
        var end = "</" + field + ">";
        var startPosition = xml.indexOf(start, position) + start.length();
        if (startPosition < 0) {
            return false;
        }
        var endPosition = xml.indexOf(end, position);
        if (endPosition < 0) {
            return false;
        }
        action.accept(Map.entry(field, xml.substring(startPosition, endPosition).trim()));
        index++;
        position = endPosition + end.length();
        return index < fields.length;
    }
}

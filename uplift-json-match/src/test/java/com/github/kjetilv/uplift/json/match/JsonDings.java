package com.github.kjetilv.uplift.json.match;

import com.github.kjetilv.uplift.json.Json;

final class JsonDings {

    public static String write(Object json) {
        try {
            return Json.INSTANCE.write(json);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write: " + json, e);
        }
    }

    static Object json(
        String content
    ) {
        try {
            return Json.INSTANCE.read(content);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse: " + content, e);
        }
    }

    static Object jsonObject(
        String content
    ) {
        try {
            return Json.INSTANCE.read(content);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse: " + content, e);
        }
    }

    static Object map(String content) {
        try {
            return Json.INSTANCE.read(content);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse: " + content, e);
        }
    }

    private JsonDings() {
    }
}

package com.github.kjetilv.uplift.asynchttp.rere;

import java.lang.foreign.MemorySegment;

import static com.github.kjetilv.uplift.asynchttp.rere.RequestLine.Method.*;
import static com.github.kjetilv.uplift.asynchttp.rere.Utils.string;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;

public record RequestLine(MemorySegment memorySegment, int urlIndex, int versionIndex, int lineBreak) {

    public String method() {
        return string(memorySegment, 0, methodLength());
    }

    public String url() {
        return string(memorySegment, urlIndex, urlLength());
    }

    public String version() {
        return string(memorySegment, versionIndex, versionLength());
    }

    public Method getMethod() {
        return switch (charAt(0)) {
            case 'G' -> check(3, GET);
            case 'P' -> switch (charAt(1)) {
                case 'O' -> check(4, POST);
                case 'U' -> check(3, PUT);
                default -> throw new IllegalStateException("Not a valid method: " + this);
            };
            case 'H' -> check(4, HEAD);
            case 'O' -> check(7, OPTIONS);
            case 'D' -> check(6, DELETE);
            default -> throw new IllegalStateException("Not a valid method: " + this);
        };
    }

    private Method check(int length, Method method) {
        if (methodLength() == length) {
            return method;
        }
        throw new IllegalStateException("Not a valid method: " + this);
    }

    private int charAt(int offset) {
        var ch = (int) memorySegment.get(JAVA_BYTE, offset);
        return ch < 'a'
            ? ch
            : ch - DELTA;
    }

    private int methodLength() {
        return urlIndex - 1;
    }

    private int urlLength() {
        return versionIndex - urlIndex - 1;
    }

    private int versionLength() {
        return lineBreak - versionIndex - 1;
    }

    private static final int DELTA = 'a' - 'A';

    @Override
    public String toString() {
        return method() + " " + url() + " " + version();
    }

    public enum Method {
        GET, POST, PUT, DELETE, OPTIONS, HEAD
    }
}

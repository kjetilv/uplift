package com.github.kjetilv.uplift.jmh;

import module java.base;
import com.github.kjetilv.uplift.edamame.MapsMemoizers;

import static com.github.kjetilv.uplift.hash.HashKind.K128;

/// Prints the heap retained by the parsed data set, and by the same data memoized, after full GC. With the
/// argument `copy`, the memoized maps are taken with [com.github.kjetilv.uplift.edamame.MapsMemoizer#maps(boolean)].
public final class MemoizerFootprint {

    static void main(String[] args) {
        var copy = args.length > 0 && args[0].equals("copy");
        MemoizerData.maps(1);
        var empty = used();
        var maps = MemoizerData.maps(Integer.MAX_VALUE);
        var parsed = used();

        var memoizer = MapsMemoizers.<Integer, K128>create(K128);
        for (var i = 0; i < maps.size(); i++) {
            memoizer.put(i, maps.get(i));
        }
        var memoized = copy ? memoizer.maps(true) : memoizer.maps();
        memoizer = null;
        maps = null;
        var after = used();

        IO.println("Lines: " + memoized.size());
        IO.println("Heap before loading: " + empty / 1024 / 1024 + " MB");
        IO.println("Parsed maps retained: " + parsed / 1024 / 1024 + " MB");
        IO.println("Memoized maps retained (" + (copy ? "maps(true)" : "maps()") + "): " + after / 1024 / 1024 + " MB");
        Objects.requireNonNull(memoized.get(0));
    }

    private static long used() {
        for (var i = 0; i < 4; i++) {
            System.gc();
        }
        var runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private MemoizerFootprint() {
    }
}

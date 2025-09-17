package com.github.kjetilv.uplift.edam.patterns;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class Cycles {

    @SafeVarargs
    static <T> List<T> find(T... ts) {
        return find(List.of(ts));
    }

    static <T> List<T> find(List<T> ts) {
        if (ts == null) {
            throw new IllegalArgumentException("Null list");
        }
        int len = ts.size();
        if (len == 0) {
            throw new IllegalArgumentException("Empty list");
        }
        if (len == 1) { // Trivial case, single item
            return ts;
        }
        if (distinct(ts) == 1) { // Single-item list
            // Single-value list
            return List.of(ts.getFirst());
        }
        if (isPrime(len)) { // Prime length, cannot be divided
            return ts;
        }
        for (int i = 0; SIMPLE_PRIMES[i] <= len / 2; i++) {
            int subLen = SIMPLE_PRIMES[i];
            if (len % subLen == 0) {
                List<T> prefix = ts.subList(0, subLen);
                if (multipleOf(prefix, ts)) {
                    return prefix;
                }
            }
        }
        return ts;
    }

    private Cycles() {
    }

    private static final Set<Integer> SIMPLE_PRIMES_SET = Set.of(
        2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47,
        53, 59, 61, 67, 71, 73, 79, 83, 89, 97,
        101, 103, 107, 109, 113, 127, 131, 137, 139, 149,
        151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199
    );

    private static final int[] SIMPLE_PRIMES =
        SIMPLE_PRIMES_SET.stream().mapToInt(i -> i).sorted().toArray();

    private static boolean isPrime(int len) {
        return SIMPLE_PRIMES_SET.contains(len);
    }

    private static <T> int distinct(List<T> ts) {
        return new HashSet<>(ts).size();
    }

    private static <T> boolean multipleOf(List<T> prefix, List<T> ts) {
        int len = ts.size();
        int prefixLen = prefix.size();
        for (int i = 0; i < len; i++) {
            if (prefix.get(i % prefixLen) != ts.get(i)) {
                return false;
            }
        }
        return true;
    }
}

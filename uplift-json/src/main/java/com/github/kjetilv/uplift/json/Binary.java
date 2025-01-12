package com.github.kjetilv.uplift.json;

final class Binary {

    static int search(int[] is, int i) {
        int len = is.length;
        int lower = 0;
        int upper = len - 1;
        int index = midpoint(lower, upper);
        while (index >= 0) {
            int found = is[index];
            if (found == i) {
                return index;
            }
            if (found < i) {
                lower = index + 1;
                index = recompute(lower, upper, index);
            } else {
                upper = index - 1;
                index = recompute(lower, upper, index);
            }
        }
        return index;
    }

    private Binary() {
    }

    private static int recompute(int lower, int upper, int index) {
        int newIndex = midpoint(lower, upper);
        return index == newIndex ? -1 : newIndex;
    }

    private static int midpoint(int start, int end) {
        int range = end - start;
        int middle = range / 2 + range % 2;
        return start + middle;
    }
}

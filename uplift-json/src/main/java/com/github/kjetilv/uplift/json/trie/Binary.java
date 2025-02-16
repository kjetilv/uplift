package com.github.kjetilv.uplift.json.trie;

final class Binary {

    static int search(int[] is, int i) {
        if (is.length == 0) {
            return -1;
        }
        int len = is.length;
        int lower = 0;
        int upper = len - 1;
        int index = mid(lower, upper);
        do {
            if (index == -1) {
                return -1;
            }
            int found = is[index];
            if (found == i) {
                return index;
            }
            if (found < i) {
                lower = index + 1;
            } else {
                upper = index - 1;
            }
            int newIndex = mid(lower, upper);
            if (index == newIndex) {
                return -1;
            }
            index = newIndex;
        } while (true);
    }

    private Binary() {
    }

    private static int mid(int start, int end) {
        int range = end - start;
        int middle = range / 2 + range % 2;
        return start + middle;
    }
}

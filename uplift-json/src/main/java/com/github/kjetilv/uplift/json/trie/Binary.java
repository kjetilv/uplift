package com.github.kjetilv.uplift.json.trie;

final class Binary {

    static int search(int[] is, int i) {
        if (is.length == 0) {
            return NOT_FOUND;
        }
        int len = is.length;
        int lowerIndex = 0;
        int upperIndex = len - 1;
        int currentIndex = midPoint(lowerIndex, upperIndex);
        do {
            int current = is[currentIndex];
            if (current == i) { // Found it
                return currentIndex;
            }
            if (current < i) { // Search higher part; set lower bound one higher
                lowerIndex = currentIndex + 1;
            } else { // Search lower part; set upper bound one lower
                upperIndex = currentIndex - 1;
            }
            int nextIndex = midPoint(lowerIndex, upperIndex);
            if (nextIndex == currentIndex || nextIndex == NOT_FOUND) { // Search stops
                return NOT_FOUND;
            }
            currentIndex = nextIndex;
        } while (true);
    }

    private Binary() {
    }

    private static final int NOT_FOUND = -1;

    private static int midPoint(int start, int end) {
        int range = end - start;
        return start + range / 2 + range % 2;
    }
}

package com.github.kjetilv.uplift.json.trie;

final class Binary {

    static int search(int[] ints, int target) {
        if (ints.length == 0) {
            return NOT_FOUND;
        }
        int lo = 0;
        int hi = ints.length - 1;
        while (lo <= hi) {
            int index = midPoint(hi, lo);
            int found = ints[index];
            if (found == target) {
                // Found it
                return index;
            }
            if (found < target) {
                // Search higher part; set lower bound one higher
                lo = index + 1;
            } else {
                // Search lower part; set upper bound one lower
                hi = index - 1;
            }
        }
        return NOT_FOUND;
    }

    private Binary() {
    }

    private static final int NOT_FOUND = -1;

    private static int midPoint(int hi, int lo) {
        int r = hi - lo;
        return lo + r / 2 + r % 2;
    }
}

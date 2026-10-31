package com.github.kjetilv.uplift.json.gen.tokens;

import module java.base;
import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.json.TokenResolver;

/// Resolves field names to canonical [Token.Field] instances, and unknown names to `null`.
///
/// An open addressing table with linear probing. By default, the hash samples the length and three bytes,
/// which costs the same for any name length. If the known names collide so that a run of occupied slots
/// exceeds [#MAX_RUN], and a hash of all bytes gives shorter runs, the table uses that hash instead. The
/// longest run bounds the comparisons in any lookup. Every lookup ends with a full comparison of the
/// candidate.
public final class TokenTable implements TokenResolver {

    private final Token.Field[] table;

    private final int mask;

    private final boolean sampled;

    private final int size;

    public TokenTable(Token.Field... fields) {
        this(List.of(fields));
    }

    public TokenTable(Collection<Token.Field> fields) {
        var distinct = fields.stream().distinct()
            .toList();
        var capacity = Integer.highestOneBit(Math.max(distinct.size(), 1) * LOAD_FACTOR_INVERSE - 1) << 1;
        var sampledTable = table(distinct, capacity, true);
        var sampledRun = longestRun(sampledTable);
        var fullTable = sampledRun > MAX_RUN
            ? table(distinct, capacity, false)
            : null;
        this.sampled = fullTable == null || sampledRun <= longestRun(fullTable);
        this.table = sampled ? sampledTable : fullTable;
        this.mask = capacity - 1;
        this.size = distinct.size();
    }

    @Override
    public Token.Field get(byte[] bytes) {
        return get(bytes, 0, bytes.length);
    }

    @Override
    public Token.Field get(byte[] bytes, int offset, int length) {
        var index = (sampled ? sampledHash(bytes, offset, length) : fullHash(bytes, offset, length)) & mask;
        while (true) {
            var field = table[index];
            if (field == null) {
                return null;
            }
            if (equals(bytes, offset, length, field)) {
                return field;
            }
            index = index + 1 & mask;
        }
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    @Override
    public Token.Field get(IntUnaryOperator bytes, int offset, int length) {
        var copy = new byte[length];
        for (var i = 0; i < length; i++) {
            copy[i] = (byte) bytes.applyAsInt(offset + i);
        }
        return get(copy, 0, length);
    }

    /// Longest accepted run of occupied slots for the sampled hash
    private static final int MAX_RUN = 8;

    private static final int LOAD_FACTOR_INVERSE = 8;

    private static boolean equals(
        byte[] bytes,
        int offset,
        int length,
        Token.Field field
    ) {
        return field.length() == length && Arrays.equals(
            field.bytes(),
            0,
            length,
            bytes,
            offset,
            offset + length
        );
    }

    private static Token.Field[] table(List<Token.Field> fields, int capacity, boolean sampled) {
        var table = new Token.Field[capacity];
        var mask = capacity - 1;
        for (var field : fields) {
            var bytes = field.bytes();
            var length = field.length();
            var index = (sampled ? sampledHash(bytes, 0, length) : fullHash(bytes, 0, length)) & mask;
            while (table[index] != null) {
                index = index + 1 & mask;
            }
            table[index] = field;
        }
        return table;
    }

    private static int longestRun(Token.Field[] table) {
        OptionalInt found = OptionalInt.empty();
        for (int i = 0; i < table.length; i++) {
            if (table[i] == null) {
                found = OptionalInt.of(i);
                break;
            }
        }
        var firstEmpty = found
            .orElseThrow();
        // Start after an empty slot, so runs that wrap around the end are counted whole
        var longest = 0;
        var run = 0;
        for (var i = 1; i <= table.length; i++) {
            if (table[(firstEmpty + i) % table.length] == null) {
                run = 0;
            } else {
                longest = Math.max(longest, ++run);
            }
        }
        return longest;
    }

    private static int sampledHash(byte[] bytes, int offset, int length) {
        if (length == 0) {
            return 0;
        }
        var h = length;
        h = h * 31 + bytes[offset];
        h = h * 31 + bytes[offset + (length >> 1)];
        h = h * 31 + bytes[offset + length - 1];
        return mix(h);
    }

    private static int fullHash(byte[] bytes, int offset, int length) {
        var h = length;
        for (var i = offset; i < offset + length; i++) {
            h = h * 31 + bytes[i];
        }
        return mix(h);
    }

    private static int mix(int hash) {
        var h = hash * 0x9E3779B9;
        return h ^ h >>> 16;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + size + " " + (sampled ? "sampled" : "full") + "]";
    }
}

package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.util.Bytes;

public interface BytesSource {

    int chomp();

    Bytes lexeme();

    Bytes spoolField();

    Bytes spoolString();

    Bytes spoolNumber();

    /// Reads a string value, after the opening quote. Sources may override this to avoid creating a
    /// [Bytes] instance.
    ///
    /// @return The decoded string
    default String spoolStringValue() {
        return spoolString().string();
    }

    /// Reads a field name, after the opening quote, and resolves it. Sources may override this to avoid
    /// creating a [Bytes] instance.
    ///
    /// @param resolver Resolver
    /// @return The field, or null if the resolver does not know it
    default Token.Field spoolField(TokenResolver resolver) {
        var lexeme = spoolField();
        return resolver.get(lexeme.bytes(), lexeme.offset(), lexeme.length());
    }

    /// Reads a number, starting with the character returned by the last [#chomp()], and parses it. Sources
    /// may override this to avoid creating a [Bytes] instance.
    ///
    /// @param parser Parser
    /// @param <T>    Type of parsed number
    /// @return The parsed number
    default <T> T spoolNumber(LexemeParser<T> parser) {
        var lexeme = spoolNumber();
        return parser.parse(lexeme.bytes(), lexeme.offset(), lexeme.length());
    }

    void skip(char c1, char c2, char c3);

    void skip(char c1, char c2, char c3, char c4);

    boolean done();

    /// @return Approximate byte offset of the next unread byte, or -1 if not known
    default long position() {
        return -1;
    }

    /// Parses a lexeme, given as a range of an array. The array may be reused after the call.
    ///
    /// @param <T> Type of result
    @FunctionalInterface
    interface LexemeParser<T> {

        T parse(byte[] bytes, int offset, int length);
    }
}

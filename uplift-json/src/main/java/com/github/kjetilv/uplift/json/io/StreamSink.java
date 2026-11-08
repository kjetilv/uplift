package com.github.kjetilv.uplift.json.io;

import module java.base;

/// Writes to an output stream through a small buffer. The buffer is written to the stream when it is full and
/// on [#close()], so the sink must be closed. The stream itself is not closed.
///
/// UTF-8 is encoded directly into the buffer, without intermediate strings or byte arrays. Other charsets
/// are encoded with [String#getBytes(Charset)].
///
/// Not thread-safe. This class holds mutable state, since it is the inner loop of all stream writes.
final class StreamSink extends AbstractEncodingSink {

    private final OutputStream outputStream;

    private final boolean utf8;

    private final byte[] buffer;

    private int position;

    private long flushed;

    StreamSink(OutputStream outputStream, Charset charset) {
        super(charset);
        this.outputStream = Objects.requireNonNull(outputStream, "outputStream");
        this.utf8 = charset().equals(StandardCharsets.UTF_8);
        this.buffer = new byte[BUFFER_SIZE];
    }

    @Override
    public void accept(String string) {
        if (utf8) {
            encode(string, false);
        } else {
            write(string.getBytes(charset()));
        }
    }

    @Override
    public void accept(char c) {
        if (utf8 && c < 0x80) {
            ensure(1);
            buffer[position++] = (byte) c;
        } else {
            accept(JsonStrings.string(c));
        }
    }

    @Override
    public void accept(long value) {
        if (value == Long.MIN_VALUE || !utf8) {
            accept(Long.toString(value));
            return;
        }
        ensure(MAX_LONG_LENGTH);
        var remaining = value;
        if (remaining < 0) {
            buffer[position++] = '-';
            remaining = -remaining;
        }
        var end = position + digits(remaining);
        for (var i = end - 1; i >= position; i--) {
            buffer[i] = (byte) ('0' + remaining % 10);
            remaining /= 10;
        }
        position = end;
    }

    @Override
    public void acceptEscaped(String string) {
        if (utf8) {
            encode(string, true);
        } else {
            accept(JsonStrings.escape(string));
        }
    }

    @Override
    public void acceptQuoted(String string) {
        if (utf8) {
            accept('"');
            acceptEscaped(string);
            accept('"');
        } else {
            super.acceptQuoted(string);
        }
    }

    @Override
    public void acceptField(String field) {
        if (utf8) {
            accept('"');
            acceptEscaped(field);
            accept('"');
            accept(':');
        } else {
            super.acceptField(field);
        }
    }

    @Override
    public long length() {
        return flushed + position;
    }

    @Override
    public void close() {
        flush();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[->" + outputStream + ']';
    }

    /// Encodes as UTF-8, as [String#getBytes(Charset)] does: unpaired surrogates become `?`.
    ///
    /// @param string String
    /// @param escape Whether to apply [JSON escapes][JsonStrings#escape(String)]
    private void encode(String string, boolean escape) {
        var length = string.length();
        var index = 0;
        while (index < length) {
            ensure(MAX_CHAR_LENGTH);
            var end = Math.min(length, index + (buffer.length - position) / MAX_CHAR_LENGTH);
            index = encode(string, index, end, escape);
        }
    }

    /// Encodes a segment that is known to fit in the buffer. A surrogate pair may end one char after the
    /// segment; it needs 4 bytes, less than the space reserved for its first char.
    ///
    /// @return Index of the next char to encode
    private int encode(String string, int start, int end, boolean escape) {
        var length = string.length();
        var i = start;
        for (; i < end; i++) {
            var c = string.charAt(i);
            if (c < 0x80) {
                if (escape && JsonStrings.needsEscape(c)) {
                    writeEscape(c);
                } else {
                    buffer[position++] = (byte) c;
                }
            } else if (c < 0x800) {
                buffer[position++] = (byte) (0xC0 | c >> 6);
                buffer[position++] = (byte) (0x80 | c & 0x3F);
            } else if (!Character.isSurrogate(c)) {
                buffer[position++] = (byte) (0xE0 | c >> 12);
                buffer[position++] = (byte) (0x80 | c >> 6 & 0x3F);
                buffer[position++] = (byte) (0x80 | c & 0x3F);
            } else if (Character.isHighSurrogate(c) && i + 1 < length && Character.isLowSurrogate(string.charAt(i + 1))) {
                var codePoint = Character.toCodePoint(c, string.charAt(++i));
                buffer[position++] = (byte) (0xF0 | codePoint >> 18);
                buffer[position++] = (byte) (0x80 | codePoint >> 12 & 0x3F);
                buffer[position++] = (byte) (0x80 | codePoint >> 6 & 0x3F);
                buffer[position++] = (byte) (0x80 | codePoint & 0x3F);
            } else {
                buffer[position++] = '?';
            }
        }
        return i;
    }

    private void writeEscape(char c) {
        var shortEscape = JsonStrings.shortEscape(c);
        buffer[position++] = '\\';
        if (shortEscape == null) {
            buffer[position++] = 'u';
            buffer[position++] = '0';
            buffer[position++] = '0';
            buffer[position++] = (byte) JsonStrings.hex(c >> 4);
            buffer[position++] = (byte) JsonStrings.hex(c & 0xF);
        } else {
            buffer[position++] = (byte) shortEscape.charAt(1);
        }
    }

    private void write(byte[] bytes) {
        if (bytes.length > buffer.length - position) {
            flush();
            if (bytes.length > buffer.length) {
                writeOut(bytes, bytes.length);
                return;
            }
        }
        System.arraycopy(bytes, 0, buffer, position, bytes.length);
        position += bytes.length;
    }

    private void ensure(int length) {
        if (length > buffer.length - position) {
            flush();
        }
    }

    private void flush() {
        if (position > 0) {
            writeOut(buffer, position);
            position = 0;
        }
    }

    private void writeOut(byte[] bytes, int length) {
        try {
            outputStream.write(bytes, 0, length);
            flushed += length;
        } catch (Exception e) {
            throw new IllegalStateException(this + " failed to write " + length + " bytes", e);
        }
    }

    private static int digits(long value) {
        var digits = 1;
        for (var remaining = value; remaining >= 10; remaining /= 10) {
            digits++;
        }
        return digits;
    }

    private static final int BUFFER_SIZE = 512;

    /// Longest encoding of one char: a six-character unicode escape
    private static final int MAX_CHAR_LENGTH = 6;

    /// Sign and 19 digits
    private static final int MAX_LONG_LENGTH = 20;
}

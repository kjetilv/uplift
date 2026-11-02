package com.github.kjetilv.uplift.json.io;

import module java.base;

/// Thrown for input that is not valid JSON. [ParseException][com.github.kjetilv.uplift.json.ParseException]
/// is a subtype, for valid tokens in the wrong place.
///
/// Carries the approximate byte position of the error, and the path of fields leading to it.
public class ReadException extends RuntimeException {

    private final String text;

    private final long position;

    private final List<String> fieldPath;

    public ReadException(String msg) {
        this(msg, null, -1);
    }

    public ReadException(String msg, String details) {
        this(msg, details, -1);
    }

    /// @param msg      Message
    /// @param details  Details, may be null
    /// @param position Approximate byte offset in the input, or -1 if not known
    public ReadException(String msg, String details, long position) {
        this(msg + (details == null ? "" : ": " + details), position, List.of());
    }

    protected ReadException(String text, long position, List<String> fieldPath) {
        super(text);
        this.text = text;
        this.position = position;
        this.fieldPath = List.copyOf(fieldPath);
    }

    /// @return Approximate byte offset of the error in the input, or -1 if not known
    public long position() {
        return position;
    }

    /// @return Names of the fields leading to the error, outermost first
    public List<String> fieldPath() {
        return fieldPath;
    }

    /// @param field Name of a field enclosing the error
    /// @return Equal exception, with the field added to the front of the [field path][#fieldPath()]
    public ReadException inField(String field) {
        var copy = withFieldPath(Stream.concat(Stream.of(field), fieldPath.stream()).toList());
        copy.setStackTrace(getStackTrace());
        return copy;
    }

    @Override
    public String getMessage() {
        return (fieldPath.isEmpty() ? "" : "Failed to set `" + String.join(".", fieldPath) + "`: ") +
               text +
               (position < 0 ? "" : " (near byte " + position + ")");
    }

    /// @param fieldPath New field path
    /// @return Copy of this exception, of the same type, with the new field path
    protected ReadException withFieldPath(List<String> fieldPath) {
        return new ReadException(text, position, fieldPath);
    }

    protected String text() {
        return text;
    }
}

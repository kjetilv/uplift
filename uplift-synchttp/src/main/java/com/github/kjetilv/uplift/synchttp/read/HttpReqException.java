package com.github.kjetilv.uplift.synchttp.read;

import module java.base;

/// Exceptions for reqeusts that cannot be served, because of an error by the client or an unsupported feature.
/// The server responds with [#status()], then closes the connection.
public final class HttpReqException extends RuntimeException {

    /// @param throwable Throwable
    /// @return The first [HttpReqException] in the cause chain, if any
    public static Optional<HttpReqException> find(Throwable throwable) {
        return Stream.iterate(throwable, Objects::nonNull, Throwable::getCause)
            .filter(HttpReqException.class::isInstance)
            .map(HttpReqException.class::cast)
            .findFirst();
    }

    static HttpReqException badRequest(String message) {
        return badRequest(message, null);
    }

    static HttpReqException badRequest(String message, Throwable cause) {
        return new HttpReqException(400, message, cause);
    }

    static HttpReqException tooLarge(String message) {
        return tooLarge(message, null);
    }

    static HttpReqException tooLarge(String message, Throwable cause) {
        return new HttpReqException(431, message, cause);
    }

    static HttpReqException notImplemented(String message) {
        return notImplemented(message, null);
    }

    static HttpReqException notImplemented(String message, Throwable cause) {
        return new HttpReqException(501, message, cause);
    }

    static HttpReqException versionNotSupported(String message) {
        return versionNotSupported(message, null);
    }

    static HttpReqException versionNotSupported(String message, Throwable cause) {
        return new HttpReqException(505, message, cause);
    }

    private final int status;

    HttpReqException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int status() {
        return status;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + status + ": " + getMessage() + "]";
    }
}

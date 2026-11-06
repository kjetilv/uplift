package com.github.kjetilv.uplift.synchttp.read;

import module java.base;

/// A request that cannot be served, because of an error by the client or an unsupported feature.
/// The server responds with [#status()], and closes the connection.
public final class HttpReqException extends RuntimeException {

    /// @param throwable Throwable
    /// @return The first [HttpReqException] in the cause chain, if any
    public static Optional<HttpReqException> find(Throwable throwable) {
        return Stream.iterate(throwable, Objects::nonNull, Throwable::getCause)
            .filter(HttpReqException.class::isInstance)
            .map(HttpReqException.class::cast)
            .findFirst();
    }

    private final int status;

    HttpReqException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }

    static HttpReqException badRequest(String message) {
        return new HttpReqException(400, message);
    }

    static HttpReqException tooLarge(String message) {
        return new HttpReqException(431, message);
    }

    static HttpReqException notImplemented(String message) {
        return new HttpReqException(501, message);
    }

    static HttpReqException versionNotSupported(String message) {
        return new HttpReqException(505, message);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + status + ": " + getMessage() + "]";
    }
}

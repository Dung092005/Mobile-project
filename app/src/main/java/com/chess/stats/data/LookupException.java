package com.chess.stats.data;

/** A typed lookup failure; {@link #httpCode} is 0 unless the server answered with an error status. */
public final class LookupException extends Exception {

    public final ErrorType type;
    public final int httpCode;

    public LookupException(ErrorType type) {
        this(type, 0);
    }

    public LookupException(ErrorType type, int httpCode) {
        super(type.name() + (httpCode != 0 ? " (HTTP " + httpCode + ")" : ""));
        this.type = type;
        this.httpCode = httpCode;
    }

    public static LookupException fromHttpStatus(int code) {
        return new LookupException(typeForStatus(code), code);
    }

    /** Chess.com answers 404 for unknown players and 410 for closed accounts. */
    static ErrorType typeForStatus(int code) {
        if (code == 404 || code == 410) {
            return ErrorType.NOT_FOUND;
        }
        if (code == 429) {
            return ErrorType.RATE_LIMITED;
        }
        if (code >= 500 && code <= 599) {
            return ErrorType.SERVER;
        }
        return ErrorType.HTTP;
    }
}

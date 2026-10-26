package com.chess.stats.data;

/** Every way a lookup can fail. The UI maps each value to a localized message. */
public enum ErrorType {
    EMPTY_USERNAME,
    INVALID_USERNAME,
    NOT_FOUND,
    RATE_LIMITED,
    SERVER,
    HTTP,
    NETWORK,
    TIMEOUT,
    INVALID_DATA,
    NO_STATS
}

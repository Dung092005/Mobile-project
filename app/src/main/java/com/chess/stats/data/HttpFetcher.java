package com.chess.stats.data;

/** Blocking HTTP GET. Must be called off the main thread. Swappable in unit tests. */
public interface HttpFetcher {

    /** Returns the UTF-8 body of a 2xx response, otherwise throws a typed {@link LookupException}. */
    String fetchText(String url) throws LookupException;

    /** Returns the raw body of a 2xx response, at most {@code maxBytes} long. */
    byte[] fetchBytes(String url, int maxBytes) throws LookupException;
}

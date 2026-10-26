package com.chess.stats.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** {@link HttpFetcher} built on the platform's HttpURLConnection, as in the course slides. */
public final class HttpUrlConnectionFetcher implements HttpFetcher {

    static final int CONNECT_TIMEOUT_MS = 10_000;
    static final int READ_TIMEOUT_MS = 15_000;
    /** Stats/profile JSON is a few KB; anything this large is not a valid answer. */
    static final int MAX_TEXT_BYTES = 1024 * 1024;

    /** Chess.com asks API clients to identify themselves; no personal data is sent. */
    private static final String USER_AGENT = "ChessStats-Android/1.0 (student project)";

    @Override
    public String fetchText(String url) throws LookupException {
        return new String(fetch(url, MAX_TEXT_BYTES, "application/json"), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] fetchBytes(String url, int maxBytes) throws LookupException {
        return fetch(url, maxBytes, "image/*");
    }

    private byte[] fetch(String url, int maxBytes, String accept) throws LookupException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setRequestProperty("Accept", accept);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.connect();

            int code = connection.getResponseCode();
            if (code < 200 || code > 299) {
                closeQuietly(connection.getErrorStream());
                throw LookupException.fromHttpStatus(code);
            }
            try (InputStream in = connection.getInputStream()) {
                return readLimited(in, maxBytes);
            }
        } catch (SocketTimeoutException e) {
            throw new LookupException(ErrorType.TIMEOUT);
        } catch (TooLargeException e) {
            throw new LookupException(ErrorType.INVALID_DATA);
        } catch (IOException | RuntimeException e) {
            // UnknownHost, refused connection, TLS failure, bad URL, missing permission...
            throw new LookupException(ErrorType.NETWORK);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /** Reads the whole stream, refusing to buffer more than {@code maxBytes}. */
    static byte[] readLimited(InputStream in, int maxBytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int total = 0;
        int read;
        while ((read = in.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new TooLargeException();
            }
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private static void closeQuietly(InputStream stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (IOException ignored) {
            // Nothing useful to do; the connection is disconnected right after.
        }
    }

    static final class TooLargeException extends IOException {
        TooLargeException() {
            super("Response exceeds size limit");
        }
    }
}

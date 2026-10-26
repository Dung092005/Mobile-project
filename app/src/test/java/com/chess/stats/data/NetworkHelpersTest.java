package com.chess.stats.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/** HTTP status mapping, bounded reads and avatar down-sampling. */
public class NetworkHelpersTest {

    @Test
    public void mapsHttpStatusesToErrorTypes() {
        assertEquals(ErrorType.NOT_FOUND, LookupException.typeForStatus(404));
        assertEquals(ErrorType.NOT_FOUND, LookupException.typeForStatus(410));
        assertEquals(ErrorType.RATE_LIMITED, LookupException.typeForStatus(429));
        assertEquals(ErrorType.SERVER, LookupException.typeForStatus(500));
        assertEquals(ErrorType.SERVER, LookupException.typeForStatus(503));
        assertEquals(ErrorType.HTTP, LookupException.typeForStatus(403));
        assertEquals(ErrorType.HTTP, LookupException.typeForStatus(301));
    }

    @Test
    public void keepsTheHttpCodeForMessages() {
        LookupException e = LookupException.fromHttpStatus(502);
        assertEquals(ErrorType.SERVER, e.type);
        assertEquals(502, e.httpCode);
    }

    @Test
    public void readLimitedReturnsTheWholeBodyWithinTheLimit() throws IOException {
        byte[] body = new byte[20_000];
        for (int i = 0; i < body.length; i++) {
            body[i] = (byte) i;
        }
        assertArrayEquals(body, HttpUrlConnectionFetcher.readLimited(new ByteArrayInputStream(body), body.length));
    }

    @Test
    public void readLimitedRefusesOversizedBodies() throws IOException {
        try {
            HttpUrlConnectionFetcher.readLimited(new ByteArrayInputStream(new byte[101]), 100);
            fail("Expected TooLargeException");
        } catch (HttpUrlConnectionFetcher.TooLargeException expected) {
            // Mapped to INVALID_DATA by the fetcher.
        }
    }

    @Test
    public void sampleSizeKeepsTheAvatarAtLeastAsLargeAsTheView() {
        assertEquals(1, AvatarDecoder.sampleSize(200, 200, 180));
        assertEquals(2, AvatarDecoder.sampleSize(400, 400, 180));
        assertEquals(4, AvatarDecoder.sampleSize(1000, 800, 180));
        assertEquals(1, AvatarDecoder.sampleSize(100, 100, 180));
        assertEquals(1, AvatarDecoder.sampleSize(500, 500, 0));
    }
}

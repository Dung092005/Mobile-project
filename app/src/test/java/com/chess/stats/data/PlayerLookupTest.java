package com.chess.stats.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerLookupTest {

    private static final String STATS = "{\"chess_blitz\":{\"last\":{\"rating\":1800,\"date\":1700000000}}}";
    private static final String PROFILE = "{\"username\":\"hikaru\",\"name\":\"Hikaru Nakamura\",\"title\":\"GM\","
            + "\"avatar\":\"https://images.chesscomfiles.com/uploads/avatar.png\"}";

    private static final String STATS_URL = "https://api.chess.com/pub/player/hikaru/stats";
    private static final String PROFILE_URL = "https://api.chess.com/pub/player/hikaru";

    @Test
    public void normalizesUsernameAndUsesLowercaseApiUrls() throws LookupException {
        FakeFetcher fetcher = new FakeFetcher().ok(STATS_URL, STATS).ok(PROFILE_URL, PROFILE);
        LookupResult result = new PlayerLookup(fetcher).lookup("  @HiKaru ");

        assertEquals("HiKaru", result.username);
        assertEquals(2, fetcher.requested.size());
        assertEquals(STATS_URL, fetcher.requested.get(0));
        assertEquals(PROFILE_URL, fetcher.requested.get(1));
        assertEquals("Hikaru Nakamura", result.displayName());
        assertEquals("hikaru", result.handle());
        assertEquals("GM", result.profile.title);
        assertFalse(result.profileFailed());
    }

    @Test
    public void profileFailureStillShowsStatsWithDefaults() throws LookupException {
        FakeFetcher fetcher = new FakeFetcher().ok(STATS_URL, STATS).fail(PROFILE_URL, ErrorType.RATE_LIMITED);
        LookupResult result = new PlayerLookup(fetcher).lookup("Hikaru");

        assertTrue(result.profileFailed());
        assertNull(result.avatarUrl());
        assertNull(result.profileJson);
        assertEquals("Hikaru", result.displayName());
        assertEquals(Integer.valueOf(1800), result.stats.find("chess_blitz").currentRating);
    }

    @Test
    public void malformedProfileIsIgnored() throws LookupException {
        FakeFetcher fetcher = new FakeFetcher().ok(STATS_URL, STATS).ok(PROFILE_URL, "not json");
        assertTrue(new PlayerLookup(fetcher).lookup("hikaru").profileFailed());
    }

    @Test
    public void statsFailureFailsTheLookupWithoutFetchingTheProfile() {
        FakeFetcher fetcher = new FakeFetcher().fail(STATS_URL, ErrorType.NOT_FOUND);
        assertLookupFails(ErrorType.NOT_FOUND, fetcher, "hikaru");
        assertEquals(1, fetcher.requested.size());
    }

    @Test
    public void statsWithoutGameTypesIsNoStats() {
        FakeFetcher fetcher = new FakeFetcher().ok(STATS_URL, "{\"tactics\":{}}");
        assertLookupFails(ErrorType.NO_STATS, fetcher, "hikaru");
    }

    @Test
    public void invalidInputNeverReachesTheNetwork() {
        FakeFetcher fetcher = new FakeFetcher();
        assertLookupFails(ErrorType.EMPTY_USERNAME, fetcher, "   ");
        assertLookupFails(ErrorType.INVALID_USERNAME, fetcher, "../../etc");
        assertTrue(fetcher.requested.isEmpty());
    }

    @Test
    public void restoreRebuildsTheSameResultFromSavedJson() throws LookupException {
        FakeFetcher fetcher = new FakeFetcher().ok(STATS_URL, STATS).ok(PROFILE_URL, PROFILE);
        LookupResult original = new PlayerLookup(fetcher).lookup("hikaru");

        LookupResult restored = LookupResult.restore(original.username, original.statsJson, original.profileJson);
        assertEquals(original.displayName(), restored.displayName());
        assertEquals(original.avatarUrl(), restored.avatarUrl());
        assertEquals(original.stats.modes.size(), restored.stats.modes.size());
    }

    @Test
    public void restoreReturnsNullForUnusableState() {
        assertNull(LookupResult.restore(null, STATS, null));
        assertNull(LookupResult.restore("hikaru", null, null));
        assertNull(LookupResult.restore("hikaru", "{broken", null));
        // A broken saved profile only drops the profile.
        assertTrue(LookupResult.restore("hikaru", STATS, "{broken").profileFailed());
    }

    private static void assertLookupFails(ErrorType expected, FakeFetcher fetcher, String username) {
        try {
            new PlayerLookup(fetcher).lookup(username);
            fail("Expected " + expected);
        } catch (LookupException e) {
            assertEquals(expected, e.type);
        }
    }

    /** Returns canned responses per URL and records what was requested. */
    private static final class FakeFetcher implements HttpFetcher {
        final List<String> requested = new ArrayList<>();
        private final Map<String, String> bodies = new HashMap<>();
        private final Map<String, ErrorType> failures = new HashMap<>();

        FakeFetcher ok(String url, String body) {
            bodies.put(url, body);
            return this;
        }

        FakeFetcher fail(String url, ErrorType type) {
            failures.put(url, type);
            return this;
        }

        @Override
        public String fetchText(String url) throws LookupException {
            requested.add(url);
            if (failures.containsKey(url)) {
                throw new LookupException(failures.get(url));
            }
            if (!bodies.containsKey(url)) {
                throw new LookupException(ErrorType.NETWORK);
            }
            return bodies.get(url);
        }

        @Override
        public byte[] fetchBytes(String url, int maxBytes) throws LookupException {
            throw new LookupException(ErrorType.NETWORK);
        }
    }
}

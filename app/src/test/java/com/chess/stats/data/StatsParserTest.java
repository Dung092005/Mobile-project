package com.chess.stats.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatsParserTest {

    /** Trimmed from a real /stats response, keys deliberately out of display order. */
    private static final String FULL = "{"
            + "\"chess_daily\":{\"last\":{\"rating\":2239,\"date\":1770563021,\"rd\":103},"
            + "\"best\":{\"rating\":2464,\"date\":1397136740,\"game\":\"https://www.chess.com/game/daily/84604826\"},"
            + "\"record\":{\"win\":73,\"loss\":11,\"draw\":4,\"time_per_move\":32755,\"timeout_percent\":0}},"
            + "\"chess960_daily\":{\"last\":{\"rating\":1231,\"date\":1444458214,\"rd\":230},"
            + "\"record\":{\"win\":1,\"loss\":2,\"draw\":0}},"
            + "\"chess_bullet\":{\"last\":{\"rating\":3420,\"date\":1792259894,\"rd\":27},"
            + "\"record\":{\"win\":17129,\"loss\":2514,\"draw\":1172}},"
            + "\"chess_rapid\":{\"last\":{\"rating\":2838,\"date\":1786796329,\"rd\":44},"
            + "\"best\":{\"rating\":2927,\"date\":1645902514,\"game\":\"https://www.chess.com/game/live/141332265952\"},"
            + "\"record\":{\"win\":205,\"loss\":69,\"draw\":220},"
            + "\"tournament\":{\"count\":12,\"withdraw\":1,\"points\":40,\"highest_finish\":2}},"
            + "\"chess_blitz\":{\"last\":{\"rating\":3484,\"date\":1792512544,\"rd\":36},"
            + "\"record\":{\"win\":35987,\"loss\":5538,\"draw\":4373}},"
            + "\"fide\":2814,"
            + "\"tactics\":{\"highest\":{\"rating\":2730,\"date\":1389043258},\"lowest\":{\"rating\":2730,\"date\":1389043258}},"
            + "\"puzzle_rush\":{\"best\":{\"total_attempts\":126,\"score\":123}}"
            + "}";

    @Test
    public void ordersRapidBlitzBulletDailyThenOthers() throws LookupException {
        PlayerStats stats = StatsParser.parse(FULL);
        assertEquals(Arrays.asList("chess_rapid", "chess_blitz", "chess_bullet", "chess_daily", "chess960_daily"), keys(stats));
    }

    @Test
    public void readsRatingsDatesLinkAndRecord() throws LookupException {
        GameModeStats rapid = StatsParser.parse(FULL).find("chess_rapid");
        assertNotNull(rapid);
        assertEquals(Integer.valueOf(2838), rapid.currentRating);
        assertEquals(Long.valueOf(1786796329L), rapid.currentDate);
        assertEquals(Integer.valueOf(2927), rapid.bestRating);
        assertEquals(Long.valueOf(1645902514L), rapid.bestDate);
        assertEquals("https://www.chess.com/game/live/141332265952", rapid.bestGameUrl);
        assertEquals(Integer.valueOf(205), rapid.wins);
        assertEquals(Integer.valueOf(220), rapid.draws);
        assertEquals(Integer.valueOf(69), rapid.losses);
        assertTrue(rapid.hasCompleteRecord());
    }

    @Test
    public void splitsMainModesFromOtherModes() throws LookupException {
        PlayerStats stats = StatsParser.parse(FULL);
        assertEquals(Arrays.asList("chess_rapid", "chess_blitz", "chess_bullet", "chess_daily"), keys(stats.mainModes()));
        assertEquals(Arrays.asList("chess960_daily"), keys(stats.otherModes()));
    }

    @Test
    public void accountWideBlocksAreNotGameTypes() throws LookupException {
        PlayerStats stats = StatsParser.parse(FULL);
        assertNull(stats.find("tactics"));
        assertNull(stats.find("puzzle_rush"));
        assertNull(stats.find("fide"));
    }

    @Test
    public void missingFieldsStayNullInsteadOfZero() throws LookupException {
        PlayerStats stats = StatsParser.parse("{\"chess_blitz\":{\"last\":{\"rating\":1500}}}");
        GameModeStats blitz = stats.find("chess_blitz");
        assertEquals(Integer.valueOf(1500), blitz.currentRating);
        assertNull(blitz.currentDate);
        assertNull(blitz.bestRating);
        assertNull(blitz.bestGameUrl);
        assertFalse(blitz.hasRecord());
        assertFalse(blitz.hasCompleteRecord());
        assertEquals(0L, blitz.totalGames());
    }

    @Test
    public void wrongTypesAndNullsAreTreatedAsMissing() throws LookupException {
        GameModeStats rapid = StatsParser.parse("{\"chess_rapid\":{"
                + "\"last\":{\"rating\":\"1500\",\"date\":null},"
                + "\"best\":{\"rating\":-5,\"game\":null},"
                + "\"record\":{\"win\":3,\"draw\":1.5,\"loss\":null}}}").find("chess_rapid");
        assertNull(rapid.currentRating);
        assertNull(rapid.currentDate);
        assertNull(rapid.bestRating);
        assertNull(rapid.bestGameUrl);
        assertEquals(Integer.valueOf(3), rapid.wins);
        assertNull(rapid.draws);
        assertNull(rapid.losses);
        assertFalse(rapid.hasCompleteRecord());
        // Only the known count is added up; unknown counts are not guessed.
        assertEquals(3L, rapid.totalGames());
    }

    @Test
    public void rejectsUnsafeOrForeignBestGameLinks() throws LookupException {
        assertNull(bestGameUrl("http://www.chess.com/game/live/1"));
        assertNull(bestGameUrl("https://evil.example/game/1"));
        assertNull(bestGameUrl("https://chess.com.evil.example/game/1"));
        assertNull(bestGameUrl("javascript:alert(1)"));
        assertEquals("https://chess.com/game/live/1", bestGameUrl("https://chess.com/game/live/1"));
    }

    @Test
    public void skipsGameTypesWithoutRatingOrGames() throws LookupException {
        PlayerStats stats = StatsParser.parse("{"
                + "\"chess_daily\":{\"record\":{\"win\":0,\"draw\":0,\"loss\":0}},"
                + "\"chess_bullet\":{},"
                + "\"chess_rapid\":{\"last\":{\"rating\":1200}}}");
        assertEquals(Arrays.asList("chess_rapid"), keys(stats));
    }

    @Test
    public void unknownChessKeysFollowKnownOnesAlphabetically() throws LookupException {
        PlayerStats stats = StatsParser.parse("{"
                + "\"chess_zz\":{\"last\":{\"rating\":1}},"
                + "\"chess_aa\":{\"last\":{\"rating\":1}},"
                + "\"chess_daily\":{\"last\":{\"rating\":1}}}");
        assertEquals(Arrays.asList("chess_daily", "chess_aa", "chess_zz"), keys(stats));
    }

    @Test
    public void totalGamesAddsWinsDrawsAndLosses() throws LookupException {
        GameModeStats mode = StatsParser.parse(
                "{\"chess_blitz\":{\"record\":{\"win\":3,\"draw\":1,\"loss\":0}}}").find("chess_blitz");
        assertTrue(mode.hasRecord());
        assertEquals(4L, mode.totalGames());
    }

    @Test
    public void playerWithOnlyOtherModesHasNoMainModes() throws LookupException {
        PlayerStats stats = StatsParser.parse("{\"chess960_daily\":{\"last\":{\"rating\":1300}}}");
        assertTrue(stats.mainModes().isEmpty());
        assertEquals(Arrays.asList("chess960_daily"), keys(stats.otherModes()));
    }

    @Test
    public void playerWithoutChessStatsFailsWithNoStats() {
        assertFails(ErrorType.NO_STATS, "{}");
        assertFails(ErrorType.NO_STATS, "{\"tactics\":{},\"lessons\":{},\"puzzle_rush\":{}}");
    }

    @Test
    public void malformedJsonFailsWithInvalidData() {
        assertFails(ErrorType.INVALID_DATA, "");
        assertFails(ErrorType.INVALID_DATA, null);
        assertFails(ErrorType.INVALID_DATA, "<html>502 Bad Gateway</html>");
        assertFails(ErrorType.INVALID_DATA, "{\"chess_rapid\":");
        assertFails(ErrorType.INVALID_DATA, "[1,2,3]");
    }

    private static String bestGameUrl(String url) throws LookupException {
        return StatsParser.parse("{\"chess_rapid\":{\"best\":{\"rating\":1500,\"game\":\"" + url + "\"}}}")
                .find("chess_rapid").bestGameUrl;
    }

    private static List<String> keys(PlayerStats stats) {
        return keys(stats.modes);
    }

    private static List<String> keys(List<GameModeStats> modes) {
        List<String> keys = new ArrayList<>();
        for (GameModeStats mode : modes) {
            keys.add(mode.key);
        }
        return keys;
    }

    private static void assertFails(ErrorType expected, String json) {
        try {
            StatsParser.parse(json);
            fail("Expected " + expected);
        } catch (LookupException e) {
            assertEquals(expected, e.type);
        }
    }
}

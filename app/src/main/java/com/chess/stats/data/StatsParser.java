package com.chess.stats.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/** Turns the Chess.com stats JSON into {@link PlayerStats}. */
public final class StatsParser {

    private StatsParser() {
    }

    /**
     * @throws LookupException INVALID_DATA when the text is not a JSON object,
     *                         NO_STATS when it contains no game type with usable data.
     */
    public static PlayerStats parse(String json) throws LookupException {
        JSONObject root = parseObject(json);

        List<GameModeStats> modes = new ArrayList<>();
        Iterator<String> keys = root.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            // Only chess game types; skips "tactics", "lessons", "puzzle_rush", "fide".
            if (!key.startsWith("chess")) {
                continue;
            }
            JSONObject modeJson = root.optJSONObject(key);
            if (modeJson == null) {
                continue;
            }
            GameModeStats mode = parseMode(key, modeJson);
            if (mode.hasUsefulData()) {
                modes.add(mode);
            }
        }
        if (modes.isEmpty()) {
            throw new LookupException(ErrorType.NO_STATS);
        }
        Collections.sort(modes, DISPLAY_ORDER);
        return new PlayerStats(modes);
    }

    static JSONObject parseObject(String json) throws LookupException {
        if (json == null || json.trim().isEmpty()) {
            throw new LookupException(ErrorType.INVALID_DATA);
        }
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            throw new LookupException(ErrorType.INVALID_DATA);
        }
    }

    /** Reads the "last" (current), "best" and "record" blocks of one game type. */
    private static GameModeStats parseMode(String key, JSONObject json) {
        GameModeStats.Builder b = new GameModeStats.Builder(key);

        JSONObject last = json.optJSONObject("last");
        b.currentRating = JsonValues.nonNegativeInt(last, "rating");
        b.currentDate = JsonValues.epochSeconds(last, "date");

        JSONObject best = json.optJSONObject("best");
        b.bestRating = JsonValues.nonNegativeInt(best, "rating");
        b.bestDate = JsonValues.epochSeconds(best, "date");
        b.bestGameUrl = JsonValues.httpsUrl(best, "game", true);

        JSONObject record = json.optJSONObject("record");
        b.wins = JsonValues.nonNegativeInt(record, "win");
        b.draws = JsonValues.nonNegativeInt(record, "draw");
        b.losses = JsonValues.nonNegativeInt(record, "loss");
        return b.build();
    }

    /** Main modes in MAIN_MODE_KEYS order, then any other game type alphabetically. */
    private static final Comparator<GameModeStats> DISPLAY_ORDER = (a, b) -> {
        int rankA = rank(a.key);
        int rankB = rank(b.key);
        return rankA != rankB ? Integer.compare(rankA, rankB) : a.key.compareTo(b.key);
    };

    private static int rank(String key) {
        int index = PlayerStats.MAIN_MODE_KEYS.indexOf(key);
        return index >= 0 ? index : PlayerStats.MAIN_MODE_KEYS.size();
    }
}

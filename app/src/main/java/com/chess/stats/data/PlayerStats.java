package com.chess.stats.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Parsed result of /pub/player/{username}/stats. */
public final class PlayerStats {

    /** The popular game types shown first on the results screen, in this order. */
    public static final List<String> MAIN_MODE_KEYS = Collections.unmodifiableList(Arrays.asList(
            "chess_rapid", "chess_blitz", "chess_bullet", "chess_daily"));

    /** Game types with usable data: main modes first, then any others alphabetically. */
    public final List<GameModeStats> modes;

    PlayerStats(List<GameModeStats> modes) {
        this.modes = Collections.unmodifiableList(modes);
    }

    public GameModeStats find(String key) {
        if (key == null) {
            return null;
        }
        for (GameModeStats mode : modes) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return null;
    }

    /** Rapid, Blitz, Bullet and Daily (only those the player has data for). */
    public List<GameModeStats> mainModes() {
        List<GameModeStats> result = new ArrayList<>();
        for (GameModeStats mode : modes) {
            if (isMainMode(mode.key)) {
                result.add(mode);
            }
        }
        return result;
    }

    /** Everything else, e.g. Daily Chess960. Shown in the collapsible "Other modes" section. */
    public List<GameModeStats> otherModes() {
        List<GameModeStats> result = new ArrayList<>();
        for (GameModeStats mode : modes) {
            if (!isMainMode(mode.key)) {
                result.add(mode);
            }
        }
        return result;
    }

    public static boolean isMainMode(String key) {
        return MAIN_MODE_KEYS.contains(key);
    }
}

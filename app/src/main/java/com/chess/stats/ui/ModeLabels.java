package com.chess.stats.ui;

import android.content.res.Resources;

import com.chess.stats.R;

import java.util.Locale;

/** Display names and icons for Chess.com game-type keys such as "chess_rapid". */
public final class ModeLabels {

    private ModeLabels() {
    }

    public static String name(Resources res, String key) {
        switch (key) {
            case "chess_rapid":
                return res.getString(R.string.mode_rapid);
            case "chess_blitz":
                return res.getString(R.string.mode_blitz);
            case "chess_bullet":
                return res.getString(R.string.mode_bullet);
            case "chess_daily":
                return res.getString(R.string.mode_daily);
            case "chess960_daily":
                return res.getString(R.string.mode_daily_960);
            default:
                return prettify(key);
        }
    }

    public static int icon(String key) {
        if (key.startsWith("chess960")) {
            return R.drawable.ic_mode_960;
        }
        if (key.endsWith("rapid")) {
            return R.drawable.ic_mode_rapid;
        }
        if (key.endsWith("blitz")) {
            return R.drawable.ic_mode_blitz;
        }
        if (key.endsWith("bullet")) {
            return R.drawable.ic_mode_bullet;
        }
        if (key.endsWith("daily")) {
            return R.drawable.ic_mode_daily;
        }
        return R.drawable.ic_mode_generic;
    }

    /** "chess_bughouse" -> "Bughouse", "chess960_live" -> "Chess960 Live". */
    static String prettify(String key) {
        String text = key.startsWith("chess_") ? key.substring("chess_".length()) : key;
        StringBuilder out = new StringBuilder();
        for (String word : text.split("_")) {
            if (word.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
        }
        return out.length() > 0 ? out.toString() : key;
    }
}

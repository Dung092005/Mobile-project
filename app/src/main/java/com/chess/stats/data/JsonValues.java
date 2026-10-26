package com.chess.stats.data;

import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * Defensive readers for API JSON. Each returns null when a field is missing, JSON null, the wrong
 * type or out of range, so the UI can show "no data" instead of a made-up value.
 */
final class JsonValues {

    private JsonValues() {
    }

    /** A whole, non-negative number that fits in an int. */
    static Integer nonNegativeInt(JSONObject object, String key) {
        if (object == null) {
            return null;
        }
        Object value = object.opt(key);
        if (!(value instanceof Number)) {
            return null;
        }
        double number = ((Number) value).doubleValue();
        if (Double.isNaN(number) || number < 0 || number > Integer.MAX_VALUE || number != Math.rint(number)) {
            return null;
        }
        return (int) number;
    }

    /** A Unix timestamp in seconds; 0 and negatives mean "unknown". */
    static Long epochSeconds(JSONObject object, String key) {
        if (object == null) {
            return null;
        }
        Object value = object.opt(key);
        if (!(value instanceof Number)) {
            return null;
        }
        double number = ((Number) value).doubleValue();
        if (Double.isNaN(number) || number <= 0 || number > Long.MAX_VALUE / 1000L) {
            return null;
        }
        return (long) number;
    }

    /** A trimmed, non-empty string. JSON null (which optString would turn into "null") is ignored. */
    static String text(JSONObject object, String key) {
        if (object == null) {
            return null;
        }
        Object value = object.opt(key);
        if (!(value instanceof String)) {
            return null;
        }
        String trimmed = ((String) value).trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** An https URL, optionally restricted to chess.com and its subdomains. */
    static String httpsUrl(JSONObject object, String key, boolean chessComOnly) {
        String value = text(object, key);
        if (value == null) {
            return null;
        }
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || !"https".equals(scheme.toLowerCase(Locale.ROOT)) || host == null) {
                return null;
            }
            if (chessComOnly && !isChessComHost(host.toLowerCase(Locale.ROOT))) {
                return null;
            }
            return value;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    static JSONObject child(JSONObject object, String key) {
        return object == null ? null : object.optJSONObject(key);
    }

    private static boolean isChessComHost(String host) {
        return host.equals("chess.com") || host.endsWith(".chess.com");
    }
}

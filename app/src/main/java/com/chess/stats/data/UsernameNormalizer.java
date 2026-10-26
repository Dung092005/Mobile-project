package com.chess.stats.data;

import java.util.Locale;
import java.util.regex.Pattern;

/** Cleans up what the user typed into a Chess.com username that is safe to put in an API path. */
public final class UsernameNormalizer {

    /** Chess.com usernames use letters, digits, '_' and '-'. Anything else could alter the URL path. */
    private static final Pattern VALID = Pattern.compile("[A-Za-z0-9_-]{1,50}");

    private UsernameNormalizer() {
    }

    /**
     * Trims surrounding whitespace (including non-breaking spaces from pasted text) and drops one
     * leading '@'. Returns an empty string for null or blank input. Letter case is kept for display.
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String value = trim(raw);
        if (value.startsWith("@")) {
            value = trim(value.substring(1));
        }
        return value;
    }

    public static boolean isValid(String normalized) {
        return normalized != null && VALID.matcher(normalized).matches();
    }

    /** Normalizes and validates in one step, failing with EMPTY_USERNAME or INVALID_USERNAME. */
    public static String requireValid(String raw) throws LookupException {
        String username = normalize(raw);
        if (username.isEmpty()) {
            throw new LookupException(ErrorType.EMPTY_USERNAME);
        }
        if (!isValid(username)) {
            throw new LookupException(ErrorType.INVALID_USERNAME);
        }
        return username;
    }

    /** The API expects lowercase usernames. */
    public static String toApiPath(String normalized) {
        return normalized.toLowerCase(Locale.ROOT);
    }

    private static String trim(String value) {
        int start = 0;
        int end = value.length();
        while (start < end && isSpace(value.charAt(start))) {
            start++;
        }
        while (end > start && isSpace(value.charAt(end - 1))) {
            end--;
        }
        return value.substring(start, end);
    }

    private static boolean isSpace(char c) {
        return Character.isWhitespace(c) || Character.isSpaceChar(c);
    }
}

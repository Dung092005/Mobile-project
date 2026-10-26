package com.chess.stats.ui;

import java.util.Locale;

/** Turns the profile's country URL (e.g. ".../pub/country/VN") into a code, a name and a flag. */
public final class CountryHelper {

    private static final Locale VIETNAMESE = Locale.forLanguageTag("vi");

    private CountryHelper() {
    }

    /** "https://api.chess.com/pub/country/VN" -> "VN". Returns null when there is no valid 2-letter code. */
    public static String extractCountryCode(String countryUrl) {
        if (countryUrl == null) {
            return null;
        }
        String code = countryUrl.substring(countryUrl.lastIndexOf('/') + 1).toUpperCase(Locale.ROOT);
        return code.matches("[A-Z]{2}") ? code : null;
    }

    /**
     * Vietnamese country name from Java's built-in Locale data, e.g. "VN" -> "Việt Nam".
     * Returns null for codes Locale does not know (Chess.com uses XX, XE... for non-countries).
     */
    public static String countryName(String code) {
        String name = new Locale.Builder().setRegion(code).build().getDisplayCountry(VIETNAMESE);
        return name.isEmpty() || name.equals(code) ? null : name;
    }

    /**
     * "VN" -> 🇻🇳. A flag emoji is just two "regional indicator" letters:
     * 'A' becomes U+1F1E6, 'B' becomes U+1F1E7 ... so each letter is shifted by the same offset.
     */
    public static String countryCodeToFlagEmoji(String code) {
        int offset = 0x1F1E6 - 'A';
        return new String(Character.toChars(code.charAt(0) + offset))
                + new String(Character.toChars(code.charAt(1) + offset));
    }
}

package com.chess.stats.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class CountryHelperTest {

    @Test
    public void extractsTwoLetterCodeFromCountryUrl() {
        assertEquals("VN", CountryHelper.extractCountryCode("https://api.chess.com/pub/country/VN"));
        assertEquals("US", CountryHelper.extractCountryCode("https://api.chess.com/pub/country/us"));
    }

    @Test
    public void rejectsMissingOrMalformedCountryUrl() {
        assertNull(CountryHelper.extractCountryCode(null));
        assertNull(CountryHelper.extractCountryCode(""));
        assertNull(CountryHelper.extractCountryCode("https://api.chess.com/pub/country/"));
        assertNull(CountryHelper.extractCountryCode("https://api.chess.com/pub/country/VNM"));
        assertNull(CountryHelper.extractCountryCode("https://api.chess.com/pub/country/1A"));
    }

    @Test
    public void buildsFlagEmojiFromRegionalIndicatorLetters() {
        assertEquals("🇻🇳", CountryHelper.countryCodeToFlagEmoji("VN")); // flag of Vietnam
        assertEquals("🇺🇸", CountryHelper.countryCodeToFlagEmoji("US")); // flag of the USA
    }

    @Test
    public void namesCountriesInVietnamese() {
        assertEquals("Việt Nam", CountryHelper.countryName("VN"));
    }

    @Test
    public void chessComPseudoCountriesHaveNoName() {
        assertNull(CountryHelper.countryName("XX")); // "International" on Chess.com
        assertNull(CountryHelper.countryName("XE")); // England
    }
}

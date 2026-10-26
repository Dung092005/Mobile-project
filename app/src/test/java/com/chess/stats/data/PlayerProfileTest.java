package com.chess.stats.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class PlayerProfileTest {

    @Test
    public void readsDisplayedFields() throws LookupException {
        PlayerProfile profile = PlayerProfile.parse("{\"username\":\"hikaru\",\"name\":\" Hikaru Nakamura \","
                + "\"title\":\"GM\",\"avatar\":\"https://images.chesscomfiles.com/a.png\","
                + "\"country\":\"https://api.chess.com/pub/country/US\",\"followers\":1}");
        assertEquals("hikaru", profile.username);
        assertEquals("Hikaru Nakamura", profile.name);
        assertEquals("GM", profile.title);
        assertEquals("https://images.chesscomfiles.com/a.png", profile.avatarUrl);
        assertEquals("https://api.chess.com/pub/country/US", profile.countryUrl);
    }

    @Test
    public void missingBlankOrNullFieldsAreNull() throws LookupException {
        PlayerProfile profile = PlayerProfile.parse("{\"username\":\"x\",\"name\":\"  \",\"title\":null}");
        assertNull(profile.name);
        assertNull(profile.title);
        assertNull(profile.avatarUrl);
    }

    @Test
    public void ignoresNonHttpsAvatars() throws LookupException {
        assertNull(PlayerProfile.parse("{\"avatar\":\"http://example.com/a.png\"}").avatarUrl);
        assertNull(PlayerProfile.parse("{\"avatar\":\"file:///sdcard/a.png\"}").avatarUrl);
        assertNull(PlayerProfile.parse("{\"avatar\":42}").avatarUrl);
    }

    @Test
    public void malformedJsonIsInvalidData() {
        try {
            PlayerProfile.parse("{\"username\":");
            fail("Expected INVALID_DATA");
        } catch (LookupException e) {
            assertEquals(ErrorType.INVALID_DATA, e.type);
        }
    }
}

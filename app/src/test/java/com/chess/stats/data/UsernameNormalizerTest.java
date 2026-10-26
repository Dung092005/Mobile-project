package com.chess.stats.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class UsernameNormalizerTest {

    /** Non-breaking space, common when a username is copied from a web page. */
    private static final String NBSP = String.valueOf((char) 0x00A0);

    @Test
    public void trimsWhitespaceAndOneLeadingAt() {
        assertEquals("Hikaru", UsernameNormalizer.normalize("  @Hikaru \n"));
        assertEquals("hikaru", UsernameNormalizer.normalize("@ hikaru"));
    }

    @Test
    public void trimsNonBreakingSpacesFromPastedText() {
        assertEquals("magnus", UsernameNormalizer.normalize(NBSP + "@magnus" + NBSP + "\t"));
    }

    @Test
    public void blankOrAtOnlyInputBecomesEmpty() {
        assertEquals("", UsernameNormalizer.normalize(null));
        assertEquals("", UsernameNormalizer.normalize("   "));
        assertEquals("", UsernameNormalizer.normalize(" @ "));
    }

    @Test
    public void onlyOneAtIsRemoved() {
        String normalized = UsernameNormalizer.normalize("@@hikaru");
        assertEquals("@hikaru", normalized);
        assertFalse(UsernameNormalizer.isValid(normalized));
    }

    @Test
    public void acceptsChessComUsernameCharacters() {
        assertTrue(UsernameNormalizer.isValid("Magnus_Carlsen-2"));
    }

    @Test
    public void rejectsCharactersThatWouldChangeTheApiPath() {
        assertFalse(UsernameNormalizer.isValid("hi karu"));
        assertFalse(UsernameNormalizer.isValid("../stats"));
        assertFalse(UsernameNormalizer.isValid("name?x=1"));
        assertFalse(UsernameNormalizer.isValid("t" + (char) 0x00EA + "n"));
        assertFalse(UsernameNormalizer.isValid(""));
    }

    @Test
    public void requireValidReportsTheRightErrorType() {
        assertError(ErrorType.EMPTY_USERNAME, " @ ");
        assertError(ErrorType.INVALID_USERNAME, "a/b");
    }

    @Test
    public void apiPathIsLowercase() throws LookupException {
        assertEquals("hikaru", UsernameNormalizer.toApiPath(UsernameNormalizer.requireValid("@HiKaRu")));
    }

    private static void assertError(ErrorType expected, String input) {
        try {
            UsernameNormalizer.requireValid(input);
            fail("Expected " + expected + " for '" + input + "'");
        } catch (LookupException e) {
            assertEquals(expected, e.type);
        }
    }
}

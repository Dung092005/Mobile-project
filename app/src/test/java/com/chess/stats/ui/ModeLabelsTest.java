package com.chess.stats.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Names for game types the app has no dedicated label for. */
public class ModeLabelsTest {

    @Test
    public void prettifiesUnknownKeys() {
        assertEquals("Bughouse", ModeLabels.prettify("chess_bughouse"));
        assertEquals("Chess960 Live", ModeLabels.prettify("chess960_live"));
        assertEquals("King Of The Hill", ModeLabels.prettify("chess_king_of_the_hill"));
    }

    @Test
    public void fallsBackToTheRawKeyWhenNothingIsLeft() {
        assertEquals("chess_", ModeLabels.prettify("chess_"));
    }
}

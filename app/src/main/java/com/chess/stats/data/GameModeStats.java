package com.chess.stats.data;

/**
 * Statistics for one Chess.com game type (e.g. "chess_rapid"). Every value comes straight from
 * the API; a null field means the API did not provide it.
 */
public final class GameModeStats {

    public final String key;

    public final Integer currentRating;
    public final Long currentDate;

    public final Integer bestRating;
    public final Long bestDate;
    public final String bestGameUrl;

    public final Integer wins;
    public final Integer draws;
    public final Integer losses;

    private GameModeStats(Builder b) {
        key = b.key;
        currentRating = b.currentRating;
        currentDate = b.currentDate;
        bestRating = b.bestRating;
        bestDate = b.bestDate;
        bestGameUrl = b.bestGameUrl;
        wins = b.wins;
        draws = b.draws;
        losses = b.losses;
    }

    /** True when the API returned at least one win/draw/loss count. */
    public boolean hasRecord() {
        return wins != null || draws != null || losses != null;
    }

    /** True only when the API supplied all three counts needed for a reliable ratio. */
    public boolean hasCompleteRecord() {
        return wins != null && draws != null && losses != null;
    }

    /** Sum of the known counts; missing counts are treated as unknown, not as games played. */
    public long totalGames() {
        return value(wins) + value(draws) + value(losses);
    }

    /** A game type is worth showing only if it has a rating or at least one recorded game. */
    public boolean hasUsefulData() {
        return currentRating != null || bestRating != null || totalGames() > 0;
    }

    private static long value(Integer number) {
        return number == null ? 0L : number;
    }

    static final class Builder {
        private final String key;
        Integer currentRating;
        Long currentDate;
        Integer bestRating;
        Long bestDate;
        String bestGameUrl;
        Integer wins;
        Integer draws;
        Integer losses;

        Builder(String key) {
            this.key = key;
        }

        GameModeStats build() {
            return new GameModeStats(this);
        }
    }
}

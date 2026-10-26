package com.chess.stats.data;

/**
 * A successful lookup. The raw JSON is kept so the Activity can save and restore the result
 * (such as rotation) without another network request.
 */
public final class LookupResult {

    public final String username;
    public final PlayerStats stats;
    public final String statsJson;
    /** Null when the optional profile request failed. */
    public final PlayerProfile profile;
    public final String profileJson;

    LookupResult(String username, PlayerStats stats, String statsJson, PlayerProfile profile, String profileJson) {
        this.username = username;
        this.stats = stats;
        this.statsJson = statsJson;
        this.profile = profile;
        this.profileJson = profileJson;
    }

    public boolean profileFailed() {
        return profile == null;
    }

    /** Profile name, else the profile's username, else the name the user typed. */
    public String displayName() {
        if (profile != null && profile.name != null) {
            return profile.name;
        }
        return handle();
    }

    /** Username with the capitalization Chess.com reports, if known. */
    public String handle() {
        return profile != null && profile.username != null ? profile.username : username;
    }

    public String avatarUrl() {
        return profile == null ? null : profile.avatarUrl;
    }

    /** Rebuilds a result from saved JSON. Returns null if the saved stats can no longer be parsed. */
    public static LookupResult restore(String username, String statsJson, String profileJson) {
        if (username == null || statsJson == null) {
            return null;
        }
        try {
            PlayerStats stats = StatsParser.parse(statsJson);
            PlayerProfile profile = null;
            if (profileJson != null) {
                try {
                    profile = PlayerProfile.parse(profileJson);
                } catch (LookupException e) {
                    profileJson = null;
                }
            }
            return new LookupResult(username, stats, statsJson, profile, profileJson);
        } catch (LookupException e) {
            return null;
        }
    }
}

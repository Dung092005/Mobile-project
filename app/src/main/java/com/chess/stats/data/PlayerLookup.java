package com.chess.stats.data;

/**
 * The blocking lookup flow: stats are required, the profile is optional.
 * Has no Android dependencies so it can be unit-tested with a fake {@link HttpFetcher}.
 */
public final class PlayerLookup {

    static final String API_BASE = "https://api.chess.com/pub/player/";

    private final HttpFetcher fetcher;

    public PlayerLookup(HttpFetcher fetcher) {
        this.fetcher = fetcher;
    }

    public LookupResult lookup(String rawUsername) throws LookupException {
        String username = UsernameNormalizer.requireValid(rawUsername);
        String path = UsernameNormalizer.toApiPath(username);

        // Required: any failure here (404, 429, timeout, bad JSON, no stats) fails the lookup.
        String statsJson = fetcher.fetchText(statsUrl(path));
        PlayerStats stats = StatsParser.parse(statsJson);

        // Optional: a broken profile must not hide valid statistics.
        PlayerProfile profile = null;
        String profileJson = null;
        try {
            profileJson = fetcher.fetchText(profileUrl(path));
            profile = PlayerProfile.parse(profileJson);
        } catch (LookupException e) {
            profileJson = null;
        }
        return new LookupResult(username, stats, statsJson, profile, profileJson);
    }

    static String statsUrl(String apiPath) {
        return API_BASE + apiPath + "/stats";
    }

    static String profileUrl(String apiPath) {
        return API_BASE + apiPath;
    }
}

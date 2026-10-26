package com.chess.stats.data;

import org.json.JSONObject;

/** The few fields of /pub/player/{username} that the app shows. All may be null. */
public final class PlayerProfile {

    public final String username;
    public final String name;
    public final String title;
    public final String avatarUrl;
    public final String countryUrl;

    PlayerProfile(String username, String name, String title, String avatarUrl, String countryUrl) {
        this.username = username;
        this.name = name;
        this.title = title;
        this.avatarUrl = avatarUrl;
        this.countryUrl = countryUrl;
    }

    /** @throws LookupException INVALID_DATA when the text is not a JSON object. */
    public static PlayerProfile parse(String json) throws LookupException {
        JSONObject root = StatsParser.parseObject(json);
        return new PlayerProfile(
                JsonValues.text(root, "username"),
                JsonValues.text(root, "name"),
                JsonValues.text(root, "title"),
                // Avatars are served from a CDN host, so only require https.
                JsonValues.httpsUrl(root, "avatar", false),
                JsonValues.httpsUrl(root, "country", false));
    }
}

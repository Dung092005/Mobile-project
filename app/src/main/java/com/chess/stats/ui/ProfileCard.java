package com.chess.stats.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chess.stats.R;
import com.chess.stats.data.LookupResult;

/** Binds the pinned player identity card. */
public final class ProfileCard {

    private final ImageView avatar;
    private final TextView title;
    private final TextView name;
    private final TextView flag;
    private final TextView username;
    private final TextView country;
    private final TextView modeCount;
    private final int avatarPadding;

    public ProfileCard(View card) {
        avatar = card.findViewById(R.id.player_avatar);
        title = card.findViewById(R.id.player_title);
        name = card.findViewById(R.id.player_name);
        flag = card.findViewById(R.id.player_flag);
        username = card.findViewById(R.id.player_username);
        country = card.findViewById(R.id.player_country);
        modeCount = card.findViewById(R.id.player_mode_count);
        avatarPadding = card.getResources().getDimensionPixelSize(R.dimen.avatar_padding);
        // Clip loaded photos to the oval background.
        avatar.setClipToOutline(true);
    }

    public void bind(LookupResult result) {
        Resources res = name.getResources();
        name.setText(result.displayName());
        username.setText(res.getString(R.string.username_format, result.handle()));
        bindCountry(result);
        int count = result.stats.modes.size();
        modeCount.setText(res.getQuantityString(R.plurals.mode_count, count, count));

        String playerTitle = result.profile != null ? result.profile.title : null;
        if (playerTitle != null) {
            title.setText(playerTitle);
            title.setContentDescription(res.getString(R.string.title_badge_desc, playerTitle));
            title.setVisibility(View.VISIBLE);
        } else {
            title.setVisibility(View.GONE);
        }
        showPlaceholder();
    }

    /** Shows the country name beside a location icon and puts its flag after the player name. */
    private void bindCountry(LookupResult result) {
        String countryUrl = result.profile == null ? null : result.profile.countryUrl;
        String code = CountryHelper.extractCountryCode(countryUrl);
        String countryName = code == null ? null : CountryHelper.countryName(code);
        if (countryName == null) {
            flag.setText(null);
            flag.setVisibility(View.GONE);
            country.setVisibility(View.GONE);
            return;
        }
        Resources res = country.getResources();
        flag.setText(CountryHelper.countryCodeToFlagEmoji(code));
        flag.setVisibility(View.VISIBLE);
        country.setText(countryName);
        // TalkBack reads "Quốc gia: Việt Nam" instead of the emoji's letters.
        country.setContentDescription(res.getString(R.string.player_country, countryName));
        country.setVisibility(View.VISIBLE);
    }

    public void showPlaceholder() {
        avatar.setPadding(avatarPadding, avatarPadding, avatarPadding, avatarPadding);
        avatar.setImageResource(R.drawable.ic_person);
        avatar.setContentDescription(avatar.getResources().getString(R.string.player_avatar_placeholder_desc));
    }

    public void showAvatar(Bitmap bitmap, String playerName) {
        avatar.setPadding(0, 0, 0, 0);
        avatar.setImageBitmap(bitmap);
        avatar.setContentDescription(avatar.getResources().getString(R.string.player_avatar_desc, playerName));
    }

    /** Decode size for downloaded avatars: the on-screen size in pixels. */
    public int avatarSizePx() {
        return avatar.getResources().getDimensionPixelSize(R.dimen.avatar_size);
    }
}

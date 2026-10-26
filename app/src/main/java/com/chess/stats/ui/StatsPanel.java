package com.chess.stats.ui;

import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chess.stats.R;
import com.chess.stats.data.GameModeStats;
import com.chess.stats.data.PlayerStats;

import java.util.Collections;
import java.util.List;

/**
 * Fills the scrollable statistics area.
 * <ul>
 *   <li>{@link #showModes}: one tile per game type; Rapid/Blitz/Bullet/Daily first,
 *       any other type behind the "Other modes" toggle.</li>
 *   <li>{@link #bindMode}: rating card and win/draw/loss card for the selected type.</li>
 * </ul>
 */
public final class StatsPanel {

    public interface Listener {
        void onModeSelected(String key);

        void onOpenLink(String url);
    }

    private final Resources res;
    private final LayoutInflater inflater;
    private final Formatters format;
    private final Listener listener;

    private final TextView notice;
    private final GridLayout modeGrid;
    private final Button otherModesToggle;
    private final GridLayout otherModeGrid;
    private final TextView detailHeading;
    private final TextView currentRating;
    private final TextView currentDate;
    private final TextView bestRating;
    private final TextView bestDate;
    private final Button bestGameLink;
    private final View recordColumns;
    private final TextView wins;
    private final TextView draws;
    private final TextView losses;
    private final View recordBar;
    private final View barWins;
    private final View barDraws;
    private final View barLosses;
    private final TextView recordTotal;
    private final TextView recordWinRate;
    private final TextView recordSummary;
    private final TextView recordEmpty;

    private int otherModeCount;
    private boolean otherModesExpanded;

    public StatsPanel(View root, Formatters format, Listener listener) {
        this.res = root.getResources();
        this.inflater = LayoutInflater.from(root.getContext());
        this.format = format;
        this.listener = listener;

        notice = root.findViewById(R.id.results_notice);
        modeGrid = root.findViewById(R.id.mode_grid);
        otherModesToggle = root.findViewById(R.id.other_modes_toggle);
        otherModeGrid = root.findViewById(R.id.other_mode_grid);
        detailHeading = root.findViewById(R.id.detail_heading);
        currentRating = root.findViewById(R.id.current_rating);
        currentDate = root.findViewById(R.id.current_date);
        bestRating = root.findViewById(R.id.best_rating);
        bestDate = root.findViewById(R.id.best_date);
        bestGameLink = root.findViewById(R.id.best_game_link);
        recordColumns = root.findViewById(R.id.record_columns);
        wins = root.findViewById(R.id.record_wins);
        draws = root.findViewById(R.id.record_draws);
        losses = root.findViewById(R.id.record_losses);
        recordBar = root.findViewById(R.id.record_bar);
        barWins = root.findViewById(R.id.record_bar_wins);
        barDraws = root.findViewById(R.id.record_bar_draws);
        barLosses = root.findViewById(R.id.record_bar_losses);
        recordTotal = root.findViewById(R.id.record_total);
        recordWinRate = root.findViewById(R.id.record_win_rate);
        recordSummary = root.findViewById(R.id.record_summary);
        recordEmpty = root.findViewById(R.id.record_empty);

        // Round the ends of the coloured segments to match the track.
        recordBar.setClipToOutline(true);
        otherModesToggle.setOnClickListener(v -> setOtherModesExpanded(!otherModesExpanded));
    }

    /** Null hides the notice. */
    public void setNotice(String message) {
        notice.setText(message);
        notice.setVisibility(message == null ? View.GONE : View.VISIBLE);
    }

    // region Game-type tiles

    public void showModes(PlayerStats stats) {
        List<GameModeStats> mainModes = stats.mainModes();
        List<GameModeStats> otherModes = stats.otherModes();
        if (mainModes.isEmpty()) {
            // A player who only plays unusual game types: show those as the main tiles.
            mainModes = otherModes;
            otherModes = Collections.emptyList();
        }
        fillGrid(modeGrid, mainModes);
        fillGrid(otherModeGrid, otherModes);

        otherModeCount = otherModes.size();
        otherModesToggle.setVisibility(otherModes.isEmpty() ? View.GONE : View.VISIBLE);
        setOtherModesExpanded(false);
    }

    private void fillGrid(GridLayout grid, List<GameModeStats> modes) {
        grid.removeAllViews();
        for (GameModeStats mode : modes) {
            grid.addView(createTile(grid, mode));
        }
    }

    private View createTile(GridLayout grid, GameModeStats mode) {
        View tile = inflater.inflate(R.layout.item_mode_tile, grid, false);
        TextView name = tile.findViewById(R.id.tile_name);
        TextView rating = tile.findViewById(R.id.tile_rating);

        name.setCompoundDrawablesRelativeWithIntrinsicBounds(ModeLabels.icon(mode.key), 0, 0, 0);
        name.setText(ModeLabels.name(res, mode.key));
        if (mode.currentRating != null) {
            rating.setText(format.rating(mode.currentRating));
        } else {
            rating.setText(R.string.tile_no_rating);
            rating.setTextColor(res.getColor(R.color.muted, null));
            rating.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen.text_label));
        }

        // Equal-width columns: every tile takes weight 1 in its column.
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED),
                GridLayout.spec(GridLayout.UNDEFINED, 1f));
        params.width = 0;
        int gap = res.getDimensionPixelSize(R.dimen.space_xs);
        params.setMargins(gap, gap, gap, gap);
        tile.setLayoutParams(params);

        tile.setTag(mode.key);
        tile.setOnClickListener(v -> listener.onModeSelected((String) v.getTag()));
        return tile;
    }

    private void setOtherModesExpanded(boolean expanded) {
        otherModesExpanded = expanded;
        otherModeGrid.setVisibility(expanded && otherModeCount > 0 ? View.VISIBLE : View.GONE);
        otherModesToggle.setText(expanded
                ? res.getString(R.string.other_modes_hide)
                : res.getString(R.string.other_modes_show, otherModeCount));
    }

    private static void markSelectedTile(GridLayout grid, String selectedKey) {
        for (int i = 0; i < grid.getChildCount(); i++) {
            View tile = grid.getChildAt(i);
            tile.setSelected(selectedKey.equals(tile.getTag()));
        }
    }

    private static boolean gridContains(GridLayout grid, String key) {
        for (int i = 0; i < grid.getChildCount(); i++) {
            if (key.equals(grid.getChildAt(i).getTag())) {
                return true;
            }
        }
        return false;
    }

    // endregion

    // region Selected game type

    public void bindMode(GameModeStats mode) {
        markSelectedTile(modeGrid, mode.key);
        markSelectedTile(otherModeGrid, mode.key);
        // Keep the selected tile visible, e.g. after rotating with an "other" mode selected.
        if (gridContains(otherModeGrid, mode.key)) {
            setOtherModesExpanded(true);
        }
        detailHeading.setText(res.getString(R.string.detail_heading, ModeLabels.name(res, mode.key)));

        showRating(currentRating, currentDate, mode.currentRating, mode.currentDate,
                R.string.date_as_of, R.dimen.text_rating_big);
        showRating(bestRating, bestDate, mode.bestRating, mode.bestDate,
                R.string.date_achieved, R.dimen.text_rating_mid);
        showBestGameLink(mode.bestGameUrl);
        showRecord(mode);
    }

    /** A rating with its date, or "No data yet" in a smaller size when the API has no rating. */
    private void showRating(TextView value, TextView date, Integer rating, Long epochSeconds,
                            int dateTextRes, int ratingSizeRes) {
        if (rating != null) {
            value.setText(format.rating(rating));
            value.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(ratingSizeRes));
        } else {
            value.setText(R.string.no_data);
            value.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen.text_body));
        }
        if (epochSeconds != null) {
            date.setText(res.getString(dateTextRes, format.date(epochSeconds)));
            date.setVisibility(View.VISIBLE);
        } else {
            date.setVisibility(View.GONE);
        }
    }

    private void showBestGameLink(String url) {
        if (url != null) {
            bestGameLink.setOnClickListener(v -> listener.onOpenLink(url));
            bestGameLink.setVisibility(View.VISIBLE);
        } else {
            bestGameLink.setOnClickListener(null);
            bestGameLink.setVisibility(View.GONE);
        }
    }

    private void showRecord(GameModeStats mode) {
        if (!mode.hasRecord()) {
            showRecordMessage(R.string.no_data, false);
            return;
        }
        wins.setText(format.countOrDash(mode.wins));
        draws.setText(format.countOrDash(mode.draws));
        losses.setText(format.countOrDash(mode.losses));

        // Missing counts are unknown, so do not treat them as zero in the ratio or total.
        if (!mode.hasCompleteRecord()) {
            showRecordMessage(R.string.record_partial, true);
            return;
        }

        long total = mode.totalGames();
        if (total == 0) {
            showRecordMessage(R.string.record_empty, true);
            return;
        }
        recordColumns.setVisibility(View.VISIBLE);
        recordEmpty.setVisibility(View.GONE);

        setBarWeight(barWins, mode.wins);
        setBarWeight(barDraws, mode.draws);
        setBarWeight(barLosses, mode.losses);
        recordBar.setContentDescription(res.getString(R.string.record_bar_desc,
                format.countOrDash(mode.wins), format.countOrDash(mode.draws), format.countOrDash(mode.losses)));
        recordBar.setVisibility(View.VISIBLE);

        recordTotal.setText(res.getString(R.string.record_total, format.count(total)));
        recordTotal.setVisibility(View.VISIBLE);
        recordWinRate.setText(res.getString(R.string.record_win_rate,
                format.percent(mode.wins, total)));
        recordWinRate.setVisibility(View.VISIBLE);
        recordSummary.setVisibility(View.VISIBLE);
    }

    /** Hides the bar and total; optionally keeps the (all-zero) counts visible. */
    private void showRecordMessage(int messageRes, boolean keepCounts) {
        recordColumns.setVisibility(keepCounts ? View.VISIBLE : View.GONE);
        recordBar.setVisibility(View.GONE);
        recordTotal.setVisibility(View.GONE);
        recordWinRate.setVisibility(View.GONE);
        recordSummary.setVisibility(View.GONE);
        recordEmpty.setText(messageRes);
        recordEmpty.setVisibility(View.VISIBLE);
    }

    /** A bigger count means a wider segment; a missing count gets no width. */
    private static void setBarWeight(View segment, Integer count) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) segment.getLayoutParams();
        params.weight = count == null ? 0f : count;
        segment.setLayoutParams(params);
    }

    // endregion
}

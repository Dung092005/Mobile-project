package com.chess.stats.ui;

import android.content.res.Resources;

import com.chess.stats.R;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

/** Formats ratings, game counts and dates using the app's Vietnamese locale. */
public final class Formatters {

    private final Resources res;
    private final NumberFormat countFormat;
    private final DateFormat dateFormat;

    public Formatters(Resources res) {
        this.res = res;
        Locale locale = Locale.forLanguageTag("vi-VN");
        countFormat = NumberFormat.getIntegerInstance(locale);
        dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
    }

    /** Ratings are shown without digit grouping (1523, not 1.523). */
    public String rating(int value) {
        return String.valueOf(value);
    }

    public String count(long value) {
        return countFormat.format(value);
    }

    public String countOrDash(Integer value) {
        return value == null ? res.getString(R.string.value_missing) : count(value);
    }

    public String percent(long part, long total) {
        if (total <= 0) {
            return res.getString(R.string.value_missing);
        }
        NumberFormat percentFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        percentFormat.setMaximumFractionDigits(1);
        percentFormat.setMinimumFractionDigits(1);
        return percentFormat.format(part * 100.0 / total) + "%";
    }

    public String date(long epochSeconds) {
        return dateFormat.format(new Date(epochSeconds * 1000L));
    }
}

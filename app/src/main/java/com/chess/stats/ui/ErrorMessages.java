package com.chess.stats.ui;

import android.content.res.Resources;

import com.chess.stats.R;
import com.chess.stats.data.ErrorType;

/** Maps a lookup failure to the message shown under the username field. */
public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static String format(Resources res, ErrorType type, int httpCode, String username) {
        switch (type) {
            case EMPTY_USERNAME:
                return res.getString(R.string.error_empty_username);
            case INVALID_USERNAME:
                return res.getString(R.string.error_invalid_username);
            case NOT_FOUND:
                return res.getString(R.string.error_not_found, username);
            case RATE_LIMITED:
                return res.getString(R.string.error_rate_limited);
            case SERVER:
                return res.getString(R.string.error_server, httpCode);
            case HTTP:
                return res.getString(R.string.error_http, httpCode);
            case TIMEOUT:
                return res.getString(R.string.error_timeout);
            case NO_STATS:
                return res.getString(R.string.error_no_stats, username);
            case INVALID_DATA:
                return res.getString(R.string.error_invalid_data);
            case NETWORK:
            default:
                return res.getString(R.string.error_network);
        }
    }
}

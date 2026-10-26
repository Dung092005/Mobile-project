package com.chess.stats.ui;

import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

/**
 * Draws the app behind the status/navigation bars on every API level and reports the space the
 * bars and the keyboard take, so layouts can pad themselves instead of being covered.
 */
public final class EdgeToEdge {

    /** Pixel insets: system bars (and display cutout) plus the keyboard height in {@link #ime}. */
    public static final class Insets {
        public final int left;
        public final int top;
        public final int right;
        public final int bottom;
        public final int ime;

        Insets(int left, int top, int right, int bottom, int ime) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.ime = ime;
        }

        /** Bottom space to keep clear when the keyboard may be open. */
        public int bottomWithIme() {
            return Math.max(bottom, ime);
        }
    }

    public interface Listener {
        void onInsets(Insets insets);
    }

    private EdgeToEdge() {
    }

    @SuppressWarnings("deprecation")
    public static void enable(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    /** Dark navigation-bar icons over light content (API 26+; older versions keep the scrim). */
    @SuppressWarnings("deprecation")
    public static void setLightNavigationBar(Window window, boolean light) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                int flag = WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                controller.setSystemBarsAppearance(light ? flag : 0, flag);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            View decor = window.getDecorView();
            int flags = decor.getSystemUiVisibility();
            flags = light ? flags | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    : flags & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            decor.setSystemUiVisibility(flags);
        }
    }

    /** Calls {@code listener} whenever the bars or the keyboard change. */
    public static void observe(View root, Listener listener) {
        root.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            listener.onInsets(read(windowInsets));
            return windowInsets;
        });
        root.requestApplyInsets();
    }

    @SuppressWarnings("deprecation")
    private static Insets read(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.graphics.Insets bars = insets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            android.graphics.Insets ime = insets.getInsets(WindowInsets.Type.ime());
            return new Insets(bars.left, bars.top, bars.right, bars.bottom, ime.bottom);
        }
        // Before API 30 the system-window bottom inset already includes the keyboard.
        int stableBottom = insets.getStableInsetBottom();
        int windowBottom = insets.getSystemWindowInsetBottom();
        return new Insets(
                insets.getSystemWindowInsetLeft(),
                insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(),
                Math.min(stableBottom, windowBottom),
                windowBottom > stableBottom ? windowBottom : 0);
    }
}

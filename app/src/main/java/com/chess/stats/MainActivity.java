package com.chess.stats;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import com.chess.stats.data.ChessComRepository;
import com.chess.stats.data.ErrorType;
import com.chess.stats.data.GameModeStats;
import com.chess.stats.data.LookupException;
import com.chess.stats.data.LookupResult;
import com.chess.stats.data.UsernameNormalizer;
import com.chess.stats.ui.EdgeToEdge;
import com.chess.stats.ui.ErrorMessages;
import com.chess.stats.ui.Formatters;
import com.chess.stats.ui.ProfileCard;
import com.chess.stats.ui.StatsPanel;

/**
 * Single screen with two states: the lookup form and the results (pinned brand bar + profile
 * card above a separately scrolling statistics panel). Owns screen state and saves it so a
 * rotation keeps the current player, game type and data.
 */
public class MainActivity extends Activity implements
        ChessComRepository.LookupCallback,
        ChessComRepository.AvatarCallback,
        StatsPanel.Listener {

    private enum Screen { LANDING, LOADING, RESULTS }

    private static final String KEY_SCREEN = "screen";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_STATS_JSON = "stats_json";
    private static final String KEY_PROFILE_JSON = "profile_json";
    private static final String KEY_SELECTED_MODE = "selected_mode";
    private static final String KEY_AVATAR_FAILED = "avatar_failed";
    private static final String KEY_ERROR_TYPE = "error_type";
    private static final String KEY_ERROR_CODE = "error_code";
    /** Saved-state bundles have a size limit; real stats responses are a few KB. */
    private static final int MAX_SAVED_JSON_CHARS = 200_000;

    private ChessComRepository repository; /** lấy dữ liệu từ mạng */
    private Formatters formatters;
    /** định dạng số, ngày tháng */
    private StatsPanel statsPanel;
    /** đổ thông tin game mode, rating, win/draw/loss.*/
    private ProfileCard profileCard;
    /** đổ tên người chơi, danh hiệu, avatar */

    private View root;
    private ScrollView landing;
    private View searchCard;
    private EditText usernameInput;
    private TextView lookupMessage;
    private Button lookupButton;
    private ProgressBar lookupProgress;
    private View resultsContainer;
    private View brandBar;
    private View resultsSide;
    private ScrollView statsScroll;
    private boolean twoPane;

    private Screen screen = Screen.LANDING;
    /** Normalized username of the current or pending lookup. */
    private String username;
    private LookupResult result;
    private String selectedMode;
    private boolean avatarFailed;
    private ErrorType errorType;
    private int errorCode;
    private Object backCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(getWindow());
        setContentView(R.layout.activity_main);
        bindViews();

        formatters = new Formatters(getResources());
        statsPanel = new StatsPanel(resultsContainer, formatters, this);
        profileCard = new ProfileCard(findViewById(R.id.profile_card));
        repository = new ChessComRepository(this, this);
        wireListeners();
        EdgeToEdge.observe(root, this::applyInsets);

        if (savedInstanceState == null) {
            showLanding();
            return;
        }
        restoreState(savedInstanceState);
    }

    private void bindViews() {
        root = findViewById(R.id.root);
        landing = findViewById(R.id.landing);
        searchCard = findViewById(R.id.search_card);
        usernameInput = findViewById(R.id.username_input);
        lookupMessage = findViewById(R.id.lookup_message);
        lookupButton = findViewById(R.id.lookup_button);
        lookupProgress = findViewById(R.id.lookup_progress);
        resultsContainer = findViewById(R.id.results_container);
        brandBar = findViewById(R.id.brand_bar);
        resultsSide = findViewById(R.id.results_side);
        statsScroll = findViewById(R.id.stats_scroll);
        twoPane = getResources().getBoolean(R.bool.results_two_pane);
    }

    private void wireListeners() {
        lookupButton.setOnClickListener(v -> submitLookup());
        usernameInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean enterKey = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || enterKey) {
                submitLookup();
                return true;
            }
            return false;
        });
        usernameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                landing.post(this::keepSearchCardVisible);
            }
        });
        findViewById(R.id.change_player_button).setOnClickListener(v -> changePlayer());
    }

    // region Lookup flow

    private void submitLookup() {
        if (screen == Screen.LOADING) {
            return; // Button is disabled too; this also blocks the keyboard's search key.
        }
        String typed = usernameInput.getText().toString();
        String normalized = UsernameNormalizer.normalize(typed);
        if (normalized.isEmpty()) {
            showError(ErrorType.EMPTY_USERNAME, 0);
            return;
        }
        if (!UsernameNormalizer.isValid(normalized)) {
            showError(ErrorType.INVALID_USERNAME, 0);
            return;
        }
        hideKeyboard();
        startLookup(normalized);
    }

    private void startLookup(String normalized) {
        username = normalized;
        screen = Screen.LOADING;
        clearError();
        setLoading(true);
        repository.lookup(normalized);
    }

    @Override
    public void onLookupSuccess(LookupResult lookupResult) {
        setLoading(false);
        showResults(lookupResult, true);
    }

    @Override
    public void onLookupError(LookupException error) {
        setLoading(false);
        screen = Screen.LANDING;
        // The typed username stays in the field so it can be fixed or retried.
        showError(error.type, error.httpCode);
    }

    private void setLoading(boolean loading) {
        lookupButton.setEnabled(!loading);
        lookupButton.setText(loading ? R.string.lookup_loading : R.string.lookup_button);
        lookupProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(ErrorType type, int code) {
        errorType = type;
        errorCode = code;
        lookupMessage.setText(ErrorMessages.format(getResources(), type, code, username));
        lookupMessage.setVisibility(View.VISIBLE);
        landing.post(this::keepSearchCardVisible);
    }

    private void clearError() {
        errorType = null;
        errorCode = 0;
        lookupMessage.setVisibility(View.GONE);
    }

    // endregion

    // region Screens

    private void showLanding() {
        screen = Screen.LANDING;
        resultsContainer.setVisibility(View.GONE);
        landing.setVisibility(View.VISIBLE);
        EdgeToEdge.setLightNavigationBar(getWindow(), false);
        setBackToLanding(false);
    }

    private void showResults(LookupResult lookupResult, boolean fresh) {
        result = lookupResult;
        username = lookupResult.username;
        screen = Screen.RESULTS;
        clearError();
        if (fresh) {
            avatarFailed = false;
            selectedMode = null;
        }
        if (selectedMode == null || lookupResult.stats.find(selectedMode) == null) {
            selectedMode = lookupResult.stats.modes.get(0).key;
        }

        profileCard.bind(lookupResult);
        statsPanel.showModes(lookupResult.stats);
        statsPanel.bindMode(lookupResult.stats.find(selectedMode));
        bindAvatar();
        updateNotice();

        landing.setVisibility(View.GONE);
        if (fresh) {
            statsScroll.scrollTo(0, 0);
            resultsContainer.setAlpha(0f);
            resultsContainer.setVisibility(View.VISIBLE);
            resultsContainer.animate()
                    .alpha(1f)
                    .setDuration(getResources().getInteger(R.integer.fade_duration_ms))
                    .start();
        } else {
            resultsContainer.setVisibility(View.VISIBLE);
        }
        // Light canvas behind the navigation bar in portrait; the landscape side pane is blue.
        EdgeToEdge.setLightNavigationBar(getWindow(), !twoPane);
        setBackToLanding(true);
    }

    private void changePlayer() {
        repository.cancelAvatar();
        result = null;
        selectedMode = null;
        avatarFailed = false;
        clearError();
        showLanding();
        usernameInput.requestFocus();
        usernameInput.setSelection(usernameInput.getText().length());
    }

    @Override
    public void onModeSelected(String key) {
        if (result == null) {
            return;
        }
        GameModeStats mode = result.stats.find(key);
        if (mode != null) {
            selectedMode = key;
            statsPanel.bindMode(mode);
        }
    }

    @Override
    public void onOpenLink(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.error_no_browser, Toast.LENGTH_SHORT).show();
        }
    }

    // endregion

    // region Avatar

    private void bindAvatar() {
        repository.cancelAvatar();
        String url = result.avatarUrl();
        if (url == null) {
            profileCard.showPlaceholder();
            return;
        }
        Bitmap cached = ChessComRepository.cachedAvatar(url);
        if (cached != null) {
            profileCard.showAvatar(cached, result.displayName());
        } else if (!avatarFailed) {
            profileCard.showPlaceholder();
            repository.loadAvatar(url, profileCard.avatarSizePx());
        } else {
            profileCard.showPlaceholder();
        }
    }

    @Override
    public void onAvatarLoaded(String url, Bitmap bitmap) {
        if (screen == Screen.RESULTS && result != null && url.equals(result.avatarUrl())) {
            profileCard.showAvatar(bitmap, result.displayName());
        }
    }

    @Override
    public void onAvatarFailed(String url) {
        if (screen == Screen.RESULTS && result != null && url.equals(result.avatarUrl())) {
            avatarFailed = true;
            updateNotice();
        }
    }

    /** Non-blocking notices: the statistics stay usable either way. */
    private void updateNotice() {
        if (result != null && result.profileFailed()) {
            statsPanel.setNotice(getString(R.string.notice_profile_unavailable));
        } else if (avatarFailed) {
            statsPanel.setNotice(getString(R.string.notice_avatar_unavailable));
        } else {
            statsPanel.setNotice(null);
        }
    }

    // endregion
    // region Saved state

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_USERNAME, username);
        if (errorType != null) {
            outState.putString(KEY_ERROR_TYPE, errorType.name());
            outState.putInt(KEY_ERROR_CODE, errorCode);
        }
        Screen saved = screen;
        if (saved == Screen.RESULTS) {
            boolean fits = result != null && result.statsJson.length() <= MAX_SAVED_JSON_CHARS
                    && (result.profileJson == null || result.profileJson.length() <= MAX_SAVED_JSON_CHARS);
            if (fits) {
                outState.putString(KEY_STATS_JSON, result.statsJson);
                outState.putString(KEY_PROFILE_JSON, result.profileJson);
                outState.putString(KEY_SELECTED_MODE, selectedMode);
                outState.putBoolean(KEY_AVATAR_FAILED, avatarFailed);
            } else {
                saved = Screen.LANDING;
            }
        }
        outState.putString(KEY_SCREEN, saved.name());
    }

    private void restoreState(Bundle state) {
        username = state.getString(KEY_USERNAME);
        Screen saved = parseEnum(Screen.class, state.getString(KEY_SCREEN), Screen.LANDING);

        if (saved == Screen.RESULTS) {
            LookupResult restored = LookupResult.restore(username,
                    state.getString(KEY_STATS_JSON), state.getString(KEY_PROFILE_JSON));
            if (restored != null) {
                selectedMode = state.getString(KEY_SELECTED_MODE);
                avatarFailed = state.getBoolean(KEY_AVATAR_FAILED, false);
                showResults(restored, false);
                return;
            }
        }
        showLanding();
        if (saved == Screen.LOADING && username != null) {
            // The request belonged to the destroyed instance; ask again.
            startLookup(username);
            return;
        }
        ErrorType savedError = parseEnum(ErrorType.class, state.getString(KEY_ERROR_TYPE), null);
        if (savedError != null) {
            showError(savedError, state.getInt(KEY_ERROR_CODE, 0));
        }
    }

    private static <T extends Enum<T>> T parseEnum(Class<T> type, String name, T fallback) {
        if (name == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    // endregion

    // region Insets, keyboard and back navigation

    private void applyInsets(EdgeToEdge.Insets value) {
        // Landing: keep the form above the status bar, navigation bar and keyboard.
        pad(landing, value.left, value.top, value.right, value.bottomWithIme());
        if (twoPane) {
            // Both panes start below the status bar so its icons always sit on brand blue.
            pad(resultsContainer, value.left, value.top, value.right, 0);
            pad(brandBar, 0, 0, 0, 0);
            pad(resultsSide, 0, 0, 0, value.bottom);
        } else {
            pad(resultsContainer, value.left, 0, value.right, 0);
            pad(brandBar, 0, value.top, 0, 0);
        }
        pad(statsScroll, 0, 0, 0, value.bottom);
        if (value.ime > 0 && usernameInput.hasFocus()) {
            landing.post(this::keepSearchCardVisible);
        }
    }

    /** Adds insets on top of the padding declared in XML, which is remembered on first use. */
    private static void pad(View view, int left, int top, int right, int bottom) {
        if (view == null) {
            return;
        }
        Object tag = view.getTag(R.id.tag_initial_padding);
        Rect base;
        if (tag instanceof Rect) {
            base = (Rect) tag;
        } else {
            base = new Rect(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
            view.setTag(R.id.tag_initial_padding, base);
        }
        view.setPadding(base.left + left, base.top + top, base.right + right, base.bottom + bottom);
    }

    /** Scrolls the landing page so the whole lookup card (field, message, button) is above the keyboard. */
    private void keepSearchCardVisible() {
        if (screen == Screen.RESULTS || landing.getHeight() == 0) {
            return;
        }
        Rect card = new Rect(0, 0, searchCard.getWidth(), searchCard.getHeight());
        landing.offsetDescendantRectToMyCoords(searchCard, card);
        int visibleBottom = landing.getScrollY() + landing.getHeight() - landing.getPaddingBottom();
        int overflow = card.bottom - visibleBottom;
        if (overflow > 0) {
            landing.smoothScrollBy(0, overflow);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(usernameInput.getWindowToken(), 0);
        }
        usernameInput.clearFocus();
    }

    /** On the results screen, Back returns to the lookup form instead of closing the app. */
    private void setBackToLanding(boolean enabled) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return; // Handled in onBackPressed().
        }
        OnBackInvokedDispatcher dispatcher = getOnBackInvokedDispatcher();
        if (enabled && backCallback == null) {
            OnBackInvokedCallback callback = this::changePlayer;
            dispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback);
            backCallback = callback;
        } else if (!enabled && backCallback != null) {
            dispatcher.unregisterOnBackInvokedCallback((OnBackInvokedCallback) backCallback);
            backCallback = null;
        }
    }

    /** API 24-32 only; newer versions use the OnBackInvokedCallback from setBackToLanding(). */
    @Override
    @SuppressWarnings("deprecation")
    @SuppressLint("GestureBackNavigation")
    public void onBackPressed() {
        if (screen == Screen.RESULTS) {
            changePlayer();
        } else {
            super.onBackPressed();
        }
    }

    // endregion

    @Override
    protected void onDestroy() {
        if (repository != null) {
            repository.shutdown();
        }
        super.onDestroy();
    }
}

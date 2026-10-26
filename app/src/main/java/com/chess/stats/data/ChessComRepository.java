package com.chess.stats.data;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Runs lookups and avatar downloads on a worker thread and posts results to the main thread
 * through a {@link Handler}, following the threading model from the course slides.
 *
 * <p>Callbacks are held in fields rather than captured by the worker tasks, so {@link #shutdown()}
 * drops every reference to the Activity even while a slow request is still running.
 */
public final class ChessComRepository {

    public interface LookupCallback {
        void onLookupSuccess(LookupResult result);

        void onLookupError(LookupException error);
    }

    public interface AvatarCallback {
        void onAvatarLoaded(String url, Bitmap bitmap);

        void onAvatarFailed(String url);
    }

    private static final int MAX_AVATAR_BYTES = 1024 * 1024;

    /** Process-wide so Activity recreation does not download the avatar again. */
    private static final LruCache<String, Bitmap> AVATAR_CACHE = new LruCache<String, Bitmap>(4 * 1024 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final PlayerLookup lookup;

    // Only touched on the main thread.
    private LookupCallback lookupCallback;
    private AvatarCallback avatarCallback;
    private int lookupToken;
    private int avatarToken;

    public ChessComRepository(LookupCallback lookupCallback, AvatarCallback avatarCallback) {
        this.lookupCallback = lookupCallback;
        this.avatarCallback = avatarCallback;
        this.lookup = new PlayerLookup(new HttpUrlConnectionFetcher());
    }

    /** Starts a lookup; a newer call supersedes any result still in flight. */
    public void lookup(String username) {
        final int token = ++lookupToken;
        submit(() -> {
            LookupResult result = null;
            LookupException error = null;
            try {
                result = lookup.lookup(username);
            } catch (LookupException e) {
                error = e;
            } catch (RuntimeException e) {
                error = new LookupException(ErrorType.INVALID_DATA);
            }
            final LookupResult finalResult = result;
            final LookupException finalError = error;
            mainHandler.post(() -> deliverLookup(token, finalResult, finalError));
        }, () -> deliverLookup(token, null, new LookupException(ErrorType.NETWORK)));
    }

    public static Bitmap cachedAvatar(String url) {
        return url == null ? null : AVATAR_CACHE.get(url);
    }

    public void loadAvatar(String url, int targetPx) {
        final int token = ++avatarToken;
        submit(() -> {
            Bitmap bitmap = null;
            try {
                bitmap = AvatarDecoder.decode(new HttpUrlConnectionFetcher().fetchBytes(url, MAX_AVATAR_BYTES), targetPx);
            } catch (LookupException | RuntimeException | OutOfMemoryError ignored) {
                // Falls through to the placeholder; an avatar is never required.
            }
            if (bitmap != null) {
                AVATAR_CACHE.put(url, bitmap);
            }
            final Bitmap finalBitmap = bitmap;
            mainHandler.post(() -> deliverAvatar(token, url, finalBitmap));
        }, () -> deliverAvatar(token, url, null));
    }

    /** Ignores any avatar result that is still on its way. */
    public void cancelAvatar() {
        avatarToken++;
    }

    /** Call from Activity.onDestroy(). Pending results are dropped. */
    public void shutdown() {
        lookupCallback = null;
        avatarCallback = null;
        mainHandler.removeCallbacksAndMessages(null);
        executor.shutdownNow();
    }

    private void submit(Runnable work, Runnable onRejected) {
        try {
            executor.execute(work);
        } catch (RejectedExecutionException e) {
            mainHandler.post(onRejected);
        }
    }

    private void deliverLookup(int token, LookupResult result, LookupException error) {
        LookupCallback callback = lookupCallback;
        if (callback == null || token != lookupToken) {
            return;
        }
        if (result != null) {
            callback.onLookupSuccess(result);
        } else {
            callback.onLookupError(error != null ? error : new LookupException(ErrorType.INVALID_DATA));
        }
    }

    private void deliverAvatar(int token, String url, Bitmap bitmap) {
        AvatarCallback callback = avatarCallback;
        if (callback == null || token != avatarToken) {
            return;
        }
        if (bitmap != null) {
            callback.onAvatarLoaded(url, bitmap);
        } else {
            callback.onAvatarFailed(url);
        }
    }
}

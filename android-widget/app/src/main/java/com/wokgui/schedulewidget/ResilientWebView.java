package com.wokgui.schedulewidget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * WebView that evaluates runtime UI layers one at a time and yields to user interaction.
 * During cold assembly, MainActivity's existing hide/reveal calls are represented by an
 * opaque native mask rather than by removing the WebView from rendering. This keeps the
 * final layout measurable while preventing partially assembled UI from flashing on screen.
 */
public final class ResilientWebView extends WebView {
    private static final String CHUNK_TAG = "EDT_UI_CHUNK";
    // The branded native overlay now owns cold-start presentation. Start assembling the
    // real UI immediately and keep a short event-loop yield between layers instead of
    // adding more than a second of fixed frame delays across roughly sixty chunks.
    private static final long INITIAL_CHUNK_DELAY_MS = 0L;
    private static final long CHUNK_YIELD_MS = 4L;
    private static final long INPUT_PRIORITY_WINDOW_MS = 420L;
    private static final int STARTUP_MASK_COLOR = 0xFFF6F8FB;
    private long lastUserInteractionAt = 0L;
    private boolean startupMasked = false;

    public ResilientWebView(Context context) {
        super(context);
    }

    public ResilientWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResilientWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void setStartupMasked(boolean masked) {
        if (startupMasked == masked) return;
        startupMasked = masked;
        setForeground(masked ? new ColorDrawable(STARTUP_MASK_COLOR) : null);
        invalidate();
    }

    @Override
    public void setVisibility(int visibility) {
        // MainActivity uses INVISIBLE only as a cold-start presentation guard. Keeping the
        // WebView laid out lets Chromium finish the final geometry behind an opaque mask.
        if (visibility == View.INVISIBLE || visibility == View.GONE) {
            setStartupMasked(true);
            super.setVisibility(View.VISIBLE);
            return;
        }
        setStartupMasked(false);
        super.setVisibility(View.VISIBLE);
    }

    @Override
    public void setAlpha(float alpha) {
        // Mirror the same presentation contract for MainActivity's alpha guard without
        // suppressing WebView drawing or layout while the UI chunks are being applied.
        if (alpha <= 0.01f) setStartupMasked(true);
        else if (alpha >= 0.99f) setStartupMasked(false);
        super.setAlpha(1f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (startupMasked) return true;
        if (event != null) {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP) {
                lastUserInteractionAt = SystemClock.uptimeMillis();
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void evaluateJavascript(String script, ValueCallback<String> resultCallback) {
        if (script != null && script.contains(UiRuntimeBundle.CHUNK_MARKER)) {
            String[] chunks = script.split(java.util.regex.Pattern.quote(UiRuntimeBundle.CHUNK_MARKER), -1);
            long started = SystemClock.uptimeMillis();
            Log.i(CHUNK_TAG, "start count=" + chunks.length + " chars=" + script.length());
            postDelayed(() -> evaluateChunk(chunks, 0, started, resultCallback), INITIAL_CHUNK_DELAY_MS);
            return;
        }
        super.evaluateJavascript(script, resultCallback);
    }

    private void evaluateChunk(String[] chunks, int index, long started, ValueCallback<String> resultCallback) {
        if (index >= chunks.length) {
            Log.i(CHUNK_TAG, "complete count=" + chunks.length + " ms=" + (SystemClock.uptimeMillis() - started));
            if (resultCallback != null) resultCallback.onReceiveValue("null");
            return;
        }

        long sinceInput = SystemClock.uptimeMillis() - lastUserInteractionAt;
        if (lastUserInteractionAt > 0L && sinceInput < INPUT_PRIORITY_WINDOW_MS) {
            long delay = Math.max(CHUNK_YIELD_MS, INPUT_PRIORITY_WINDOW_MS - sinceInput);
            postDelayed(() -> evaluateChunk(chunks, index, started, resultCallback), delay);
            return;
        }

        String chunk = chunks[index];
        if (chunk == null || chunk.trim().isEmpty()) {
            postDelayed(() -> evaluateChunk(chunks, index + 1, started, resultCallback), CHUNK_YIELD_MS);
            return;
        }

        long layerStarted = SystemClock.uptimeMillis();
        super.evaluateJavascript(chunk, value -> {
            Log.i(CHUNK_TAG, "layer=" + (index + 1) + "/" + chunks.length
                    + " chars=" + chunk.length() + " ms=" + (SystemClock.uptimeMillis() - layerStarted));
            postDelayed(() -> evaluateChunk(chunks, index + 1, started, resultCallback), CHUNK_YIELD_MS);
        });
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        // Startup DOM diagnostics were useful while fixing the blank-screen bug but
        // they also caused extra JavaScript work after launch. Keep production lean.
        super.setWebViewClient(client);
    }
}

package com.wokgui.schedulewidget;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * WebView that keeps the last complete frame visible while timetable UI layers settle.
 * Runtime UI layers are evaluated one at a time and yield to user interaction.
 */
public final class ResilientWebView extends WebView {
    private static final String CHUNK_TAG = "EDT_UI_CHUNK";
    private static final long INITIAL_CHUNK_DELAY_MS = 120L;
    private static final long CHUNK_YIELD_MS = 16L;
    private static final long INPUT_PRIORITY_WINDOW_MS = 420L;
    private long lastUserInteractionAt = 0L;

    public ResilientWebView(Context context) {
        super(context);
    }

    public ResilientWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResilientWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event != null) {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP) {
                lastUserInteractionAt = SystemClock.uptimeMillis();
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void setVisibility(int visibility) {
        // Never expose the empty native activity behind the WebView while modes settle.
        boolean changed = getVisibility() != View.VISIBLE || getAlpha() < 0.99f;
        if (getVisibility() != View.VISIBLE) super.setVisibility(View.VISIBLE);
        if (getAlpha() < 0.99f) super.setAlpha(1f);
        if (changed) invalidate();
    }

    @Override
    public void setAlpha(float alpha) {
        boolean changed = getAlpha() < 0.99f || getVisibility() != View.VISIBLE;
        if (getAlpha() < 0.99f) super.setAlpha(1f);
        if (getVisibility() != View.VISIBLE) super.setVisibility(View.VISIBLE);
        if (changed) invalidate();
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

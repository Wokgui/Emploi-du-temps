package com.wokgui.schedulewidget;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * WebView that keeps the last complete frame visible while timetable UI layers settle.
 * Runtime UI layers are evaluated one at a time so a cold start never monopolizes the
 * WebView renderer with a single ~500 KB JavaScript execution.
 */
public final class ResilientWebView extends WebView {
    private static final String STARTUP_TAG = "EDT_STARTUP_STATE";
    private static final String CHUNK_TAG = "EDT_UI_CHUNK";
    private static final long CHUNK_YIELD_MS = 16L;

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
    public void setVisibility(int visibility) {
        // Never expose the empty native activity behind the WebView while modes settle.
        super.setVisibility(View.VISIBLE);
        if (getAlpha() < 0.99f) super.setAlpha(1f);
        invalidate();
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(1f);
        if (getVisibility() != View.VISIBLE) super.setVisibility(View.VISIBLE);
        invalidate();
    }

    @Override
    public void evaluateJavascript(String script, ValueCallback<String> resultCallback) {
        if (script != null && script.contains(UiRuntimeBundle.CHUNK_MARKER)) {
            String[] chunks = script.split(java.util.regex.Pattern.quote(UiRuntimeBundle.CHUNK_MARKER), -1);
            long started = SystemClock.uptimeMillis();
            Log.i(CHUNK_TAG, "start count=" + chunks.length + " chars=" + script.length());
            evaluateChunk(chunks, 0, started, resultCallback);
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

        String chunk = chunks[index];
        if (chunk == null || chunk.trim().isEmpty()) {
            postDelayed(() -> evaluateChunk(chunks, index + 1, started, resultCallback), CHUNK_YIELD_MS);
            return;
        }

        long layerStarted = SystemClock.uptimeMillis();
        super.evaluateJavascript(chunk, value -> {
            Log.i(CHUNK_TAG, "layer=" + (index + 1) + "/" + chunks.length
                    + " chars=" + chunk.length() + " ms=" + (SystemClock.uptimeMillis() - layerStarted));
            /*
             * Do not use postOnAnimation here. Immediately after an Android process
             * restart a WebView may not have produced its first compositor frame yet;
             * waiting for that frame creates a deadlock: the remaining UI layers never
             * run, so the page never becomes ready enough to draw. A tiny ordinary UI
             * delay still yields the main thread, but progresses independently of the
             * WebView compositor.
             */
            postDelayed(() -> evaluateChunk(chunks, index + 1, started, resultCallback), CHUNK_YIELD_MS);
        });
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        if (client == null) {
            super.setWebViewClient(null);
            return;
        }
        // MainActivity currently only overrides onPageFinished. Wrapping that callback
        // lets CI inspect the real DOM even when the system screenshot compositor flakes.
        super.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                client.onPageFinished(view, url);
                scheduleStartupState("page+1200", 1200);
                scheduleStartupState("page+4500", 4500);
            }
        });
    }

    private void scheduleStartupState(String label, long delayMs) {
        postDelayed(() -> {
            if (!isAttachedToWindow()) return;
            final String script = """
                    (function(){
                      try{
                        function box(sel){
                          var e=document.querySelector(sel);
                          if(!e)return null;
                          var s=getComputedStyle(e),r=e.getBoundingClientRect();
                          return {display:s.display,visibility:s.visibility,opacity:s.opacity,
                            w:Math.round(r.width),h:Math.round(r.height),top:Math.round(r.top),
                            classes:e.className||'',text:(e.innerText||'').length};
                        }
                        var m='unknown';
                        try{if(typeof mode!=='undefined')m=String(mode)}catch(e){}
                        return JSON.stringify({
                          ready:document.readyState,
                          mode:m,
                          bodyText:document.body?(document.body.innerText||'').length:-1,
                          bodyHtml:document.body?(document.body.innerHTML||'').length:-1,
                          active:Array.from(document.querySelectorAll('.view.active')).map(function(v){return v.id}),
                          nav:Array.from(document.querySelectorAll('.nav.active')).map(function(v){return v.getAttribute('data-mode')}),
                          html:box('html'),body:box('body'),header:box('.header'),context:box('.contextBar'),
                          wrap:box('main.wrap'),bottom:box('.bottom'),today:box('#viewToday'),week:box('#viewWeek'),edit:box('#viewEdit'),
                          flags:{settings:!!window.__settingsV3,fine:!!window.__fineTuneUiV1,
                            temporal:!!window.__temporalState635,startup:!!window.refreshStartupViewRecovery}
                        });
                      }catch(e){return JSON.stringify({error:String(e)})}
                    })();
                    """;
            evaluateJavascript(script, value -> Log.i(STARTUP_TAG, label + " " + value));
        }, delayMs);
    }
}

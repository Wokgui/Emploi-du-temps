package com.wokgui.schedulewidget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * WebView that keeps the last complete frame visible while timetable UI layers settle.
 *
 * Earlier versions hid the whole WebView during cold starts and mode changes. On some
 * WebView/launcher timing paths the matching reveal callback could arrive too late or
 * never repaint, leaving a completely blank activity. Keeping the current WebView frame
 * visible is both safer and less flickery: JavaScript can switch the active timetable
 * view atomically while the user never sees the native empty background.
 */
public final class ResilientWebView extends WebView {
    private static final String STARTUP_TAG = "EDT_STARTUP_STATE";

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
        // MainActivity may request INVISIBLE while a mode is settling. Never expose the
        // empty native activity behind the WebView; retain its last rendered frame.
        super.setVisibility(View.VISIBLE);
        if (getAlpha() < 0.99f) super.setAlpha(1f);
        invalidate();
    }

    @Override
    public void setAlpha(float alpha) {
        // A transparent WebView is indistinguishable from the historical blank-screen
        // failure. Mode transitions are now handled by the DOM, so transparency is not
        // needed and must never be allowed to persist.
        super.setAlpha(1f);
        if (getVisibility() != View.VISIBLE) super.setVisibility(View.VISIBLE);
        invalidate();
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        if (client == null) {
            super.setWebViewClient(null);
            return;
        }
        // MainActivity currently only overrides onPageFinished. Wrapping that callback
        // lets CI inspect the real DOM even when the screen itself is visually blank.
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

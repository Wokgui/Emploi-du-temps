package com.wokgui.schedulewidget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;

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
}

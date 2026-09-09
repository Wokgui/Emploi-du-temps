package com.wokgui.schedulewidget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;

/**
 * WebView with a last-resort visibility guard.
 *
 * MainActivity deliberately hides the WebView while the injected timetable UI is settling,
 * to avoid flashing intermediate views. If an old/slow WebView never reaches an injection
 * callback, that safety mechanism must not leave the application permanently blank.
 */
public final class ResilientWebView extends WebView {
    private static final long FAILSAFE_DELAY_MS = 1800L;

    private final Runnable failSafeReveal = () -> {
        if (getVisibility() != View.VISIBLE || getAlpha() < 0.99f) {
            super.setAlpha(1f);
            super.setVisibility(View.VISIBLE);
        }
    };

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
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            removeCallbacks(failSafeReveal);
        } else {
            removeCallbacks(failSafeReveal);
            postDelayed(failSafeReveal, FAILSAFE_DELAY_MS);
        }
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        if (alpha >= 0.99f) {
            if (getVisibility() == View.VISIBLE) removeCallbacks(failSafeReveal);
        } else {
            removeCallbacks(failSafeReveal);
            postDelayed(failSafeReveal, FAILSAFE_DELAY_MS);
        }
    }
}

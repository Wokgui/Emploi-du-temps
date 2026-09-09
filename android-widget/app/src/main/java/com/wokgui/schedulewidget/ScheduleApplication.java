package com.wokgui.schedulewidget;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/** Native safety net for timetable cold starts. */
public final class ScheduleApplication extends Application {
    private final WeakHashMap<Activity, String> requestedModes = new WeakHashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle state) {
                if (!(activity instanceof MainActivity)) return;
                String mode = activity.getIntent() == null ? null : activity.getIntent().getStringExtra("open_mode");
                requestedModes.put(activity, normalizeMode(mode));
            }

            @Override
            public void onActivityResumed(Activity activity) {
                if (!(activity instanceof MainActivity)) return;
                scheduleRecovery(activity, 1200L);
                scheduleRecovery(activity, 3200L);
            }

            @Override public void onActivityStarted(Activity activity) {}
            @Override public void onActivityPaused(Activity activity) {}
            @Override public void onActivityStopped(Activity activity) {}
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
            @Override public void onActivityDestroyed(Activity activity) { requestedModes.remove(activity); }
        });
    }

    private void scheduleRecovery(Activity activity, long delayMs) {
        View candidate = activity.findViewById(R.id.webView);
        if (!(candidate instanceof WebView)) return;
        WebView webView = (WebView) candidate;
        WeakReference<Activity> activityRef = new WeakReference<>(activity);
        String requestedMode = requestedModes.get(activity);

        webView.postDelayed(() -> {
            Activity current = activityRef.get();
            if (current == null || current.isFinishing() || current.isDestroyed()) return;
            if (webView.getVisibility() == View.VISIBLE && webView.getAlpha() >= 0.95f) return;

            String requested = normalizeMode(requestedMode);
            if (requested != null) {
                String id = "view" + capitalize(requested);
                String renderer = "today".equals(requested) ? "renderToday" : ("week".equals(requested) ? "renderWeek" : "renderEdit");
                String script = "(function(){"
                        + "try{if(typeof mode!=='undefined')mode='" + requested + "';}catch(e){}"
                        + "try{document.querySelectorAll('.view').forEach(function(v){v.classList.remove('active')});"
                        + "var target=document.getElementById('" + id + "');if(target)target.classList.add('active');"
                        + "document.querySelectorAll('.nav').forEach(function(n){n.classList.toggle('active',n.dataset.mode==='" + requested + "')});}catch(e){}"
                        + "try{if(typeof " + renderer + "==='function')" + renderer + "();else if(typeof render==='function')render();}catch(e){}"
                        + "try{if(window.refreshWeekViewStability)window.refreshWeekViewStability();if(window.refreshFineTuneUi)window.refreshFineTuneUi();}catch(e){}"
                        + "})();";
                try { webView.evaluateJavascript(script, null); } catch (Exception ignored) {}
            }

            webView.setAlpha(1f);
            webView.setVisibility(View.VISIBLE);
        }, delayMs);
    }

    private static String normalizeMode(String mode) {
        if ("today".equals(mode) || "week".equals(mode) || "edit".equals(mode)) return mode;
        return null;
    }

    private static String capitalize(String mode) {
        if (mode == null || mode.isEmpty()) return "Edit";
        return Character.toUpperCase(mode.charAt(0)) + mode.substring(1);
    }
}

package com.wokgui.schedulewidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Calendar;

/**
 * Debug-only renderer used by CI. It instantiates the production
 * UpcomingCoursesService.Factory and applies the exact widget_course_row RemoteViews.
 * Time and widget height can be frozen so temporal states are reproducible.
 */
public class WidgetPreviewActivity extends Activity {
    private Object factory;
    private Method destroyFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int heightDp = Math.max(108, Math.min(540, getIntent().getIntExtra("height_dp", 108)));
        int widthDp = Math.max(250, Math.min(390, getIntent().getIntExtra("width_dp", 350)));
        String scenario = getIntent().getStringExtra("scenario");
        Calendar now = scenarioTime(scenario == null ? "active" : scenario);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFFEEF1F5);
        setContentView(root);

        FrameLayout viewport = new FrameLayout(this);
        viewport.setBackgroundColor(0xFFFFFFFF);
        FrameLayout.LayoutParams viewportLp = new FrameLayout.LayoutParams(dp(widthDp), dp(heightDp));
        viewportLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        viewportLp.topMargin = dp(24);
        root.addView(viewport, viewportLp);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(true);
        viewport.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(0, 0, 0, 0);
        scroll.addView(rows, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        try {
            ScheduleStore.ensureInitialized(this);

            Class<?> factoryClass = Class.forName(
                    "com.wokgui.schedulewidget.UpcomingCoursesService$Factory");
            Constructor<?> ctor = factoryClass.getDeclaredConstructor(
                    Context.class, int.class, Integer.class, Calendar.class);
            ctor.setAccessible(true);
            factory = ctor.newInstance(this, AppWidgetManager.INVALID_APPWIDGET_ID, heightDp, now);

            Method create = factoryClass.getDeclaredMethod("onCreate");
            Method count = factoryClass.getDeclaredMethod("getCount");
            Method rowAt = factoryClass.getDeclaredMethod("getViewAt", int.class);
            create.setAccessible(true);
            count.setAccessible(true);
            rowAt.setAccessible(true);
            destroyFactory = factoryClass.getDeclaredMethod("onDestroy");
            destroyFactory.setAccessible(true);
            create.invoke(factory);

            int total = (Integer) count.invoke(factory);
            for (int i = 0; i < total; i++) {
                RemoteViews remote = (RemoteViews) rowAt.invoke(factory, i);
                if (remote == null) continue;
                View row = remote.apply(this, rows);
                rows.addView(row, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
            }

            if (rows.getChildCount() == 0) {
                TextView empty = new TextView(this);
                empty.setText("Aucun cours à afficher");
                empty.setGravity(Gravity.CENTER);
                rows.addView(empty, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
            }
        } catch (Throwable error) {
            TextView failure = new TextView(this);
            failure.setText("Widget preview error: " + error.getClass().getSimpleName());
            failure.setGravity(Gravity.CENTER);
            viewport.removeAllViews();
            viewport.addView(failure, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    private Calendar scenarioTime(String scenario) {
        // Thursday 10 September 2026 has all useful default states in one timetable day.
        Calendar c = Calendar.getInstance();
        c.set(2026, Calendar.SEPTEMBER, 10, 8, 30, 0);
        c.set(Calendar.MILLISECOND, 0);
        switch (scenario) {
            case "before": c.set(Calendar.HOUR_OF_DAY, 7); c.set(Calendar.MINUTE, 45); break;
            case "lunch": c.set(Calendar.HOUR_OF_DAY, 12); c.set(Calendar.MINUTE, 30); break;
            case "gap": c.set(Calendar.HOUR_OF_DAY, 15); c.set(Calendar.MINUTE, 30); break;
            case "after": c.set(Calendar.HOUR_OF_DAY, 17); c.set(Calendar.MINUTE, 30); break;
            default: c.set(Calendar.HOUR_OF_DAY, 8); c.set(Calendar.MINUTE, 30); break;
        }
        return c;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        if (factory != null && destroyFactory != null) {
            try { destroyFactory.invoke(factory); }
            catch (Exception ignored) {}
        }
        super.onDestroy();
    }
}

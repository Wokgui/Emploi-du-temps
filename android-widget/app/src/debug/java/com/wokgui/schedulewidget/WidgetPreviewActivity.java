package com.wokgui.schedulewidget;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/** Debug-only AppWidgetHost used by CI to render the real RemoteViews at exact heights. */
public class WidgetPreviewActivity extends Activity {
    private static final int HOST_ID = 7331;
    private AppWidgetHost host;
    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int heightDp = Math.max(108, Math.min(540, getIntent().getIntExtra("height_dp", 108)));
        int widthDp = Math.max(250, Math.min(390, getIntent().getIntExtra("width_dp", 350)));

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFFEEF1F5);
        setContentView(root);

        ScheduleStore.ensureInitialized(this);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        host = new AppWidgetHost(this, HOST_ID);
        host.startListening();
        widgetId = host.allocateAppWidgetId();

        Bundle options = new Bundle();
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widthDp);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widthDp);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, heightDp);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, heightDp);

        ComponentName provider = new ComponentName(this, ScheduleWidgetProvider.class);
        if (!manager.bindAppWidgetIdIfAllowed(widgetId, provider, options)) {
            TextView error = new TextView(this);
            error.setText("Widget bind permission missing");
            error.setGravity(Gravity.CENTER);
            root.addView(error, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return;
        }

        AppWidgetProviderInfo info = manager.getAppWidgetInfo(widgetId);
        AppWidgetHostView hostView = host.createView(this, widgetId, info);
        hostView.setAppWidget(widgetId, info);
        hostView.setPadding(0, 0, 0, 0);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(widthDp), dp(heightDp));
        lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        lp.topMargin = dp(24);
        root.addView(hostView, lp);

        manager.updateAppWidgetOptions(widgetId, options);
        ScheduleWidgetProvider.refreshAll(this);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        if (host != null) {
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) host.deleteAppWidgetId(widgetId);
            host.stopListening();
        }
        super.onDestroy();
    }
}

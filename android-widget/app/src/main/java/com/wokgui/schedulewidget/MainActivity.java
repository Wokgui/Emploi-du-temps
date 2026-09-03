package com.wokgui.schedulewidget;

import android.app.Activity;
import android.app.job.JobScheduler;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clearLegacySyncData();
        ScheduleStore.ensureInitialized(this);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.addJavascriptInterface(new ScheduleBridge(), "AndroidSchedule");
        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                applyOpenMode();
                injectLunchBreakUi();
            }
        });
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        applyOpenMode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.evaluateJavascript(
                    "if(window.reloadSchedule){reloadSchedule();}",
                    value -> {
                        applyOpenMode();
                        injectLunchBreakUi();
                        webView.evaluateJavascript("if(window.refreshLunchBreakUi){refreshLunchBreakUi();}", null);
                    }
            );
        }
    }

    private void clearLegacySyncData() {
        getSharedPreferences("pronote_import_v1", MODE_PRIVATE).edit().clear().apply();
        try {
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.cancel(4101);
            scheduler.cancel(4102);
        } catch (Exception ignored) {
        }
        try {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } catch (Exception ignored) {
        }
    }

    private void applyOpenMode() {
        if (webView == null || getIntent() == null) return;
        String mode = getIntent().getStringExtra("open_mode");
        if ("today".equals(mode) || "week".equals(mode) || "edit".equals(mode)) {
            webView.evaluateJavascript(
                    "if(window.setModeFromAndroid){setModeFromAndroid('" + mode + "');}",
                    null
            );
            getIntent().removeExtra("open_mode");
        }
    }

    private void injectLunchBreakUi() {
        if (webView != null) webView.evaluateJavascript(LunchBreakUi.script(), null);
    }

    private final class ScheduleBridge {
        @JavascriptInterface
        public String loadSchedule() {
            return ScheduleStore.exportJson(MainActivity.this);
        }

        @JavascriptInterface
        public void saveSchedule(String json) {
            ScheduleStore.importJson(MainActivity.this, json);
        }
    }
}

package com.wokgui.schedulewidget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScheduleStore.ensureInitialized(this);
        PronoteSyncScheduler.schedule(this);
        PronoteSyncScheduler.requestNowIfStale(this, 10L * 60L * 1000L);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.addJavascriptInterface(new ScheduleBridge(), "AndroidSchedule");
        webView.addJavascriptInterface(new PronoteAppBridge(), "AndroidPronoteApp");
        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                applyOpenMode();
                injectPronoteUi();
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
        PronoteSyncScheduler.requestNowIfStale(this, 10L * 60L * 1000L);
        if (webView != null) {
            webView.evaluateJavascript(
                    "if(window.reloadSchedule){reloadSchedule();}",
                    value -> {
                        applyOpenMode();
                        injectPronoteUi();
                        webView.evaluateJavascript("if(window.refreshPronoteUi){refreshPronoteUi();}if(window.refreshPronoteCourseUi){refreshPronoteCourseUi();}", null);
                    }
            );
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

    private void injectPronoteUi() {
        if (webView == null) return;
        webView.evaluateJavascript(PronoteUi.script(), null);
        webView.evaluateJavascript(PronoteCourseUi.script(), null);
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

    private final class PronoteAppBridge {
        @JavascriptInterface
        public void openPronote() {
            runOnUiThread(() -> startActivity(new Intent(MainActivity.this, PronoteActivity.class)));
        }

        @JavascriptInterface
        public String status() {
            return PronoteStore.getStatusJson(MainActivity.this);
        }

        @JavascriptInterface
        public String homework() {
            return PronoteStore.getSnapshot(MainActivity.this);
        }

        @JavascriptInterface
        public String courseHomework(String label) {
            return PronoteHomework.toJson(MainActivity.this, label);
        }

        @JavascriptInterface
        public void syncNow() {
            PronoteSyncScheduler.requestNow(MainActivity.this);
        }

        @JavascriptInterface
        public void clear() {
            PronoteStore.clear(MainActivity.this);
        }
    }
}

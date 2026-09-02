package com.wokgui.schedulewidget;

import android.app.Activity;
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
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new ScheduleBridge(), "AndroidSchedule");
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.evaluateJavascript("if(window.reloadSchedule){reloadSchedule();}", null);
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

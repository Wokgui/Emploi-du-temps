package fr.wokgui.planclasse;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.WHITE);
        window.setNavigationBarColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(243, 246, 248));

        // Android 15/16 force l'affichage bord-à-bord pour les apps récentes.
        // On réserve explicitement l'espace de la barre d'état / caméra et de navigation.
        if (Build.VERSION.SDK_INT >= 35) {
            int top = Math.max(systemBarSize("status_bar_height"), dp(30));
            int bottom = Math.max(systemBarSize("navigation_bar_height"), dp(12));
            webView.setPadding(0, top, 0, bottom);
            webView.setClipToPadding(false);
        }

        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setTextZoom(100);

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/index.html");
    }

    private int systemBarSize(String name) {
        int id = getResources().getIdentifier(name, "dimen", "android");
        return id > 0 ? getResources().getDimensionPixelSize(id) : 0;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

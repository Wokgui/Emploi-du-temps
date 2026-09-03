package com.wokgui.schedulewidget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

public class PronoteActivity extends Activity {
    private WebView webView;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pronote);

        status = findViewById(R.id.pronoteStatus);
        webView = findViewById(R.id.pronoteWebView);

        findViewById(R.id.pronoteBack).setOnClickListener(v -> finish());
        findViewById(R.id.pronoteImport).setOnClickListener(v -> captureCurrentPage(true));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(false);

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);

        webView.addJavascriptInterface(new CaptureBridge(), "AndroidPronoteCapture");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                } catch (Exception ignored) {
                    return false;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                status.setText(isPronotePage(url)
                        ? "Ouvre « Travail à faire », puis touche Importer"
                        : "Connexion en cours…");
                injectAutoCapture();
            }
        });

        webView.loadUrl(PronoteStore.DEFAULT_PRONOTE_URL);
    }

    @Override
    protected void onPause() {
        CookieManager.getInstance().flush();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    private boolean isPronotePage(String url) {
        try {
            String host = Uri.parse(url == null ? "" : url).getHost();
            return host != null && host.toLowerCase().endsWith("index-education.net");
        } catch (Exception ignored) {
            return false;
        }
    }

    private void injectAutoCapture() {
        captureCurrentPage(false);
    }

    private void captureCurrentPage(boolean force) {
        if (webView == null) return;
        String current = webView.getUrl();
        if (force && !isPronotePage(current)) {
            status.setText("Termine d’abord la connexion à PRONOTE");
            Toast.makeText(this, "La page PRONOTE n’est pas encore ouverte.", Toast.LENGTH_SHORT).show();
            return;
        }

        String js = "(function(){try{" +
                "const force=" + (force ? "true" : "false") + ";" +
                "const body=(document.body&&document.body.innerText?document.body.innerText:'').trim();" +
                "const needles=['3G1','3G2','3G3','3G4','4G1','4G2','4G3','4G4','5G1','5G2','5G3','5G4','6G3','6G4'];" +
                "const keyword=/travail\\s*[àa]\\s*faire|cahier\\s*de\\s*textes|devoir|travaux\\s*[àa]\\s*faire/i;" +
                "const blocks=[];const seen=new Set();" +
                "const els=document.querySelectorAll('article,section,li,tr,[role=row],[role=listitem],.liste_contenu,.conteneur,div');" +
                "for(const el of els){if(blocks.length>=40)break;let t='';try{t=(el.innerText||'').replace(/\\s+/g,' ').trim()}catch(e){}" +
                "if(t.length<18||t.length>1600)continue;const up=t.toUpperCase();" +
                "const hasClass=needles.some(n=>up.includes(n));const hasKeyword=keyword.test(t);" +
                "if(!(hasClass||hasKeyword))continue;" +
                "let childDuplicate=false;for(const c of el.children){try{const ct=(c.innerText||'').replace(/\\s+/g,' ').trim();if(ct===t){childDuplicate=true;break}}catch(e){}}" +
                "if(childDuplicate)continue;const key=t.slice(0,500);if(seen.has(key))continue;seen.add(key);blocks.push({text:t});}" +
                "const relevant=keyword.test(body)||blocks.some(b=>keyword.test(b.text));" +
                "if(force||relevant){const payload={url:location.href,title:document.title||'PRONOTE',capturedAt:Date.now(),text:body.slice(0,180000),blocks:blocks};AndroidPronoteCapture.captureDom(JSON.stringify(payload));return 'captured:'+blocks.length;}" +
                "return 'ignored';}catch(e){return 'error:'+e.message;}})();";

        webView.evaluateJavascript(js, value -> {
            if (force && value != null && value.contains("ignored")) {
                status.setText("Aucun devoir détecté sur cette page");
            }
        });
    }

    private final class CaptureBridge {
        @JavascriptInterface
        public void captureDom(String payload) {
            PronoteStore.saveSnapshot(PronoteActivity.this, payload);
            runOnUiThread(() -> {
                status.setText("Devoirs importés dans Emploi du temps");
                Toast.makeText(PronoteActivity.this, "Import PRONOTE enregistré", Toast.LENGTH_SHORT).show();
            });
        }
    }
}

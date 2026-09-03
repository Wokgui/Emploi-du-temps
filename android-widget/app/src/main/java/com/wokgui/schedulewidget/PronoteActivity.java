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
        PronoteSyncScheduler.schedule(this);
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
                        ? "Connexion reconnue. La synchronisation automatique sera ensuite faite en arrière-plan."
                        : "Connexion en cours…");
                captureCurrentPage(false);
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

    private void captureCurrentPage(boolean force) {
        if (webView == null) return;
        String current = webView.getUrl();
        if (force && !isPronotePage(current)) {
            status.setText("Termine d’abord la connexion à PRONOTE");
            Toast.makeText(this, "La page PRONOTE n’est pas encore ouverte.", Toast.LENGTH_SHORT).show();
            return;
        }

        String js = """
            (function(){
              try {
                const force = %s;
                function clean(t) {
                  let s = String(t || '');
                  s = s.replaceAll(String.fromCharCode(10), ' ')
                       .replaceAll(String.fromCharCode(13), ' ')
                       .replaceAll(String.fromCharCode(9), ' ')
                       .trim();
                  while (s.includes('  ')) s = s.replaceAll('  ', ' ');
                  return s;
                }
                const body = clean(document.body && document.body.innerText ? document.body.innerText : '');
                const needles = ['3G1','3G2','3G3','3G4','4G1','4G2','4G3','4G4','5G1','5G2','5G3','5G4','6G3','6G4'];
                const keyword = /travail *[àa] *faire|travaux *[àa] *faire|cahier *de *textes|contenu *(de *)?(la *)?(s[ée]ance|cours)|devoir|[àa] *faire *pour/i;
                const blocks = [];
                const seen = new Set();
                const selector = 'article,section,li,tr,[role=row],[role=listitem],.liste_contenu,.conteneur,[class*="travail"],[class*="cahier"],[class*="devoir"],div';
                const els = document.querySelectorAll(selector);

                function contextFor(el) {
                  const pieces = [];
                  try {
                    const ownTitle = clean(el.getAttribute('aria-label') || el.getAttribute('title') || '');
                    if (ownTitle) pieces.push(ownTitle);
                  } catch(e) {}
                  try {
                    const parent = el.closest('article,section,li,tr,[role=row],[role=listitem],.liste_contenu,.conteneur') || el.parentElement;
                    if (parent && parent !== el) {
                      const pt = clean(parent.innerText || '');
                      if (pt && pt.length <= 2400) pieces.push(pt);
                    }
                  } catch(e) {}
                  try {
                    let p = el.previousElementSibling;
                    let n = 0;
                    while (p && n < 3) {
                      const pt = clean(p.innerText || '');
                      if (pt && pt.length < 500) pieces.push(pt);
                      p = p.previousElementSibling;
                      n++;
                    }
                  } catch(e) {}
                  return clean(pieces.join(' · ')).slice(0, 2600);
                }

                for (const el of els) {
                  if (blocks.length >= 80) break;
                  let text = '';
                  try { text = clean(el.innerText || ''); } catch(e) {}
                  if (text.length < 12 || text.length > 2200) continue;
                  const context = contextFor(el);
                  const combined = clean(context + ' ' + text);
                  const upper = combined.toUpperCase();
                  const hasClass = needles.some(n => upper.includes(n));
                  const hasKeyword = keyword.test(combined);
                  if (!(hasClass || hasKeyword)) continue;

                  let childDuplicate = false;
                  for (const c of el.children) {
                    try {
                      const ct = clean(c.innerText || '');
                      if (ct === text) { childDuplicate = true; break; }
                    } catch(e) {}
                  }
                  if (childDuplicate && !hasKeyword) continue;

                  const key = combined.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 700);
                  if (seen.has(key)) continue;
                  seen.add(key);
                  blocks.push({text:text, context:context});
                }

                const relevant = keyword.test(body) || blocks.length > 0;
                if (force || relevant) {
                  if (force && blocks.length === 0 && body) {
                    blocks.push({text:body.slice(0, 12000), context:'Page PRONOTE importée'});
                  }
                  const payload = {
                    url: location.href,
                    title: document.title || 'PRONOTE',
                    capturedAt: Date.now(),
                    text: body.slice(0, 180000),
                    blocks: blocks
                  };
                  AndroidPronoteCapture.captureDom(JSON.stringify(payload));
                  return 'captured:' + blocks.length;
                }
                return 'ignored';
              } catch(e) {
                return 'error:' + e.message;
              }
            })();
            """.formatted(force ? "true" : "false");

        webView.evaluateJavascript(js, value -> {
            if (force && value != null && value.contains("ignored")) {
                status.setText("Aucun contenu de cahier de textes détecté sur cette page");
            }
        });
    }

    private final class CaptureBridge {
        @JavascriptInterface
        public void captureDom(String payload) {
            PronoteStore.mergeSnapshot(PronoteActivity.this, payload);
            PronoteStore.markSyncSuccess(PronoteActivity.this, "Session PRONOTE active");
            PronoteSyncScheduler.schedule(PronoteActivity.this);
            PronoteSyncScheduler.requestNow(PronoteActivity.this);
            runOnUiThread(() -> {
                status.setText("PRONOTE connecté. Les prochaines synchronisations seront automatiques.");
                Toast.makeText(PronoteActivity.this, "PRONOTE connecté", Toast.LENGTH_SHORT).show();
            });
        }
    }
}

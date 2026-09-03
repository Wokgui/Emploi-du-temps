package com.wokgui.schedulewidget;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.os.Looper;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PronoteSyncJobService extends JobService {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private WebView webView;
    private JobParameters params;
    private boolean sequenceStarted = false;
    private boolean finished = false;
    private int captures = 0;

    @Override
    public boolean onStartJob(JobParameters params) {
        this.params = params;
        PronoteStore.markSyncAttempt(this);
        try {
            webView = new WebView(this);
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);

            CookieManager cookies = CookieManager.getInstance();
            cookies.setAcceptCookie(true);
            cookies.setAcceptThirdPartyCookies(webView, true);

            webView.addJavascriptInterface(new BackgroundBridge(), "AndroidPronoteBackground");
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    if (!sequenceStarted) {
                        sequenceStarted = true;
                        handler.postDelayed(() -> runSequence(0), 1400L);
                    }
                }
            });
            webView.loadUrl(PronoteStore.DEFAULT_PRONOTE_URL);
            handler.postDelayed(() -> finish(false, "Synchronisation PRONOTE expirée"), 24_000L);
            return true;
        } catch (Exception e) {
            PronoteStore.markSyncError(this, "Connexion PRONOTE impossible");
            cleanup();
            return false;
        }
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        cleanup();
        return true;
    }

    private void runSequence(int step) {
        if (finished || webView == null) return;
        if (step == 0) {
            capturePage(false);
            handler.postDelayed(() -> clickText("cahier de textes", 1), 900L);
        } else if (step == 1) {
            capturePage(false);
            handler.postDelayed(() -> clickText("travail à faire", 2), 1000L);
        } else if (step == 2) {
            capturePage(false);
            handler.postDelayed(() -> finish(true,
                    captures > 0 ? "PRONOTE synchronisé automatiquement" : "Session PRONOTE active, aucun nouvel élément détecté"), 1000L);
        }
    }

    private void clickText(String target, int nextStep) {
        if (finished || webView == null) return;
        String safe = target.replace("'", "");
        String js = """
            (function(){
              try {
                const target = '%s'.toLowerCase();
                const nodes = document.querySelectorAll('a,button,[role=button],[role=menuitem],li,span,div');
                for (const el of nodes) {
                  const t = String(el.innerText || el.textContent || '').trim().toLowerCase();
                  if (!t || t.length > 90) continue;
                  if (t === target || t.includes(target)) {
                    el.click();
                    return 'clicked';
                  }
                }
                return 'not-found';
              } catch(e) { return 'error'; }
            })();
            """.formatted(safe);
        webView.evaluateJavascript(js, value -> handler.postDelayed(() -> runSequence(nextStep), 1800L));
    }

    private void capturePage(boolean force) {
        if (finished || webView == null) return;
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
                const els = document.querySelectorAll('article,section,li,tr,[role=row],[role=listitem],.liste_contenu,.conteneur,[class*="travail"],[class*="cahier"],[class*="devoir"],div');

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
                  const key = combined.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 700);
                  if (seen.has(key)) continue;
                  seen.add(key);
                  blocks.push({text:text, context:context});
                }

                const relevant = keyword.test(body) || blocks.length > 0;
                if (force || relevant) {
                  const payload = {
                    url: location.href,
                    title: document.title || 'PRONOTE',
                    capturedAt: Date.now(),
                    text: body.slice(0, 180000),
                    blocks: blocks
                  };
                  AndroidPronoteBackground.captureDom(JSON.stringify(payload));
                  return 'captured:' + blocks.length;
                }
                return 'ignored';
              } catch(e) {
                return 'error:' + e.message;
              }
            })();
            """.formatted(force ? "true" : "false");
        webView.evaluateJavascript(js, null);
    }

    private void finish(boolean success, String message) {
        if (finished) return;
        finished = true;
        if (success) PronoteStore.markSyncSuccess(this, message);
        else PronoteStore.markSyncError(this, message);
        CookieManager.getInstance().flush();
        ScheduleWidgetProvider.refreshAll(this);
        cleanup();
        if (params != null) jobFinished(params, false);
    }

    private void cleanup() {
        handler.removeCallbacksAndMessages(null);
        if (webView != null) {
            try {
                webView.stopLoading();
                webView.removeJavascriptInterface("AndroidPronoteBackground");
                webView.destroy();
            } catch (Exception ignored) {
            }
            webView = null;
        }
    }

    private final class BackgroundBridge {
        @JavascriptInterface
        public void captureDom(String payload) {
            PronoteStore.mergeSnapshot(PronoteSyncJobService.this, payload);
            captures++;
        }
    }
}

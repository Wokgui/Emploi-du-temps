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
        if (webView != null) {
            webView.evaluateJavascript(
                    "if(window.reloadSchedule){reloadSchedule();}",
                    value -> {
                        applyOpenMode();
                        injectPronoteUi();
                        webView.evaluateJavascript("if(window.refreshPronoteUi){refreshPronoteUi();}", null);
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
        String js = "(function(){try{" +
                "if(!document.getElementById('pronoteExtraStyle')){const st=document.createElement('style');st.id='pronoteExtraStyle';st.textContent='" +
                ".pronote-card{margin-top:18px;padding:16px;border-left:5px solid #08a7a5}.pronote-head{display:flex;align-items:center;justify-content:space-between;gap:10px}.pronote-title{font-weight:850;color:#111936;font-size:1.05rem}.pronote-status{color:#68738a;font-size:.86rem;margin-top:5px}.pronote-actions{display:flex;gap:8px;margin-top:12px;flex-wrap:wrap}.pronote-btn{border:1px solid #dce3eb;border-radius:8px;background:#fff;color:#1178e8;padding:10px 12px;font-weight:760}.pronote-btn.primary{background:#08a7a5;border-color:#08a7a5;color:#fff}.pronote-homework{margin-top:14px}.pronote-homework h3{margin:0 0 8px;font-size:1.02rem}.pronote-task{padding:11px 12px;border-top:1px solid #e9edf2}.pronote-task:first-of-type{border-top:0}.pronote-task-course{font-weight:800;color:#087f7d}.pronote-task-text{margin-top:4px;color:#4d596e;font-size:.88rem;line-height:1.35}.pronote-dot{display:inline-flex;align-items:center;justify-content:center;position:absolute;right:4px;top:4px;min-width:20px;height:20px;border-radius:99px;background:#08a7a5;color:#fff;font-size:10px;font-weight:850;padding:0 5px}.wc.pronote-cell{position:relative}.pronote-muted{color:#68738a;font-size:.86rem;padding:10px 0}';document.head.appendChild(st);}" +
                "const edit=document.getElementById('viewEdit');if(edit&&!document.getElementById('pronoteSettingsCard')){const card=document.createElement('section');card.id='pronoteSettingsCard';card.className='card pronote-card';card.innerHTML='<div class=\"pronote-head\"><div><div class=\"pronote-title\">PRONOTE · Travail à faire</div><div id=\"pronoteStatusText\" class=\"pronote-status\">Aucun import pour le moment</div></div></div><div class=\"pronote-actions\"><button id=\"pronoteOpen\" class=\"pronote-btn primary\" type=\"button\">Connecter / synchroniser</button><button id=\"pronoteClear\" class=\"pronote-btn\" type=\"button\">Effacer l’import</button></div>';edit.appendChild(card);document.getElementById('pronoteOpen').onclick=()=>AndroidPronoteApp.openPronote();document.getElementById('pronoteClear').onclick=()=>{AndroidPronoteApp.clear();setTimeout(()=>window.refreshPronoteUi&&window.refreshPronoteUi(),80);};}" +
                "const today=document.getElementById('viewToday');if(today&&!document.getElementById('pronoteTodayCard')){const card=document.createElement('section');card.id='pronoteTodayCard';card.className='card pronote-card pronote-homework';card.style.display='none';today.appendChild(card);}" +
                "function clean(t){return String(t||'').replace(/\\s+/g,' ').trim();}" +
                "function tokens(label){const u=String(label||'').toUpperCase();const a=u.match(/[3-6]G\\d(?:BIL)?/g)||[];const first=u.match(/[3-6]G\\d(?:-\\d)+(?:\\s|$)/);if(first)a.push(first[0].trim());return [...new Set(a)];}" +
                "function snapshot(){try{return JSON.parse(AndroidPronoteApp.homework()||'{}')}catch(e){return {}}}" +
                "function status(){try{return JSON.parse(AndroidPronoteApp.status()||'{}')}catch(e){return {}}}" +
                "function matches(label,data){const ts=tokens(label),blocks=Array.isArray(data.blocks)?data.blocks:[];if(!ts.length)return [];return blocks.filter(b=>{const u=clean(b.text).toUpperCase();return ts.some(t=>u.includes(t));});}" +
                "function snippet(t){let s=clean(t);if(s.length>260)s=s.slice(0,257)+'…';return s;}" +
                "function renderStatus(){const s=status(),el=document.getElementById('pronoteStatusText');if(!el)return;if(!s.hasData){el.textContent='Non connecté dans cette application · aucune donnée importée';return;}const d=new Date(Number(s.importedAt||0));el.textContent='Dernier import : '+d.toLocaleString('fr-FR')+' · '+Number(s.blocks||0)+' bloc(s) détecté(s)';}" +
                "function renderToday(){const card=document.getElementById('pronoteTodayCard');if(!card)return;const data=snapshot();const day=new Date().getDay()+1;let courses=[];try{courses=(window.state&&state[day]&&Array.isArray(state[day].courses))?state[day].courses:[]}catch(e){}let rows=[],used=new Set();for(const c of courses){for(const b of matches(c.label,data)){const txt=clean(b.text);if(!txt||used.has(txt))continue;used.add(txt);rows.push({course:c.label,text:txt});if(rows.length>=6)break;}if(rows.length>=6)break;}if(!rows.length){card.style.display='none';card.innerHTML='';return;}card.style.display='block';card.innerHTML='<h3>Travail à faire · PRONOTE</h3>'+rows.map(r=>'<div class=\"pronote-task\"><div class=\"pronote-task-course\">'+String(r.course).replace(/[&<>]/g,'')+'</div><div class=\"pronote-task-text\">'+snippet(r.text).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')+'</div></div>').join('');}" +
                "function renderWeek(){const data=snapshot();document.querySelectorAll('#weekGrid .pronote-dot').forEach(x=>x.remove());document.querySelectorAll('#weekGrid .wc.has').forEach(cell=>{const labelEl=cell.querySelector('.cellLabel');const label=labelEl?labelEl.textContent:cell.textContent;if(matches(label,data).length){cell.classList.add('pronote-cell');const dot=document.createElement('span');dot.className='pronote-dot';dot.textContent='P';dot.title='Travail PRONOTE détecté';cell.appendChild(dot);}});}" +
                "window.refreshPronoteUi=function(){renderStatus();renderToday();renderWeek();};" +
                "document.querySelectorAll('.nav').forEach(b=>{if(!b.dataset.pronoteHook){b.dataset.pronoteHook='1';b.addEventListener('click',()=>setTimeout(window.refreshPronoteUi,100));}});" +
                "window.refreshPronoteUi();" +
                "}catch(e){console.log('Pronote UI',e);}})();";
        webView.evaluateJavascript(js, null);
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
        public void clear() {
            PronoteStore.clear(MainActivity.this);
        }
    }
}

package com.wokgui.schedulewidget;

import android.Manifest;
import android.app.Activity;
import android.app.job.JobScheduler;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private static final int PICK_TIMETABLE_PHOTO = 5201;
    private static final int PICK_BACKUP = 5202;
    private static final int NOTIFICATION_PERMISSION = 5203;
    private WebView webView;
    private boolean forceWeekOpening = false;
    private boolean pageLoaded = false;
    private final TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forceWeekOpening = isWidgetWeekIntent(getIntent());
        clearLegacySyncData();
        ScheduleStore.ensureInitialized(this);
        ProfileStore.ensure(this);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);
        webView.setBackgroundColor(0xFFF6F8FB);
        hideWebViewUntilWeekIsReady();

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        webView.setWebChromeClient(new WebChromeClient());

        webView.addJavascriptInterface(new ScheduleBridge(), "AndroidSchedule");
        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pageLoaded = true;
                primeWeekBadge();
                if (!forceWeekOpening) applyOpenMode();
                injectPersonalizationUi();
            }
        });
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        forceWeekOpening = isWidgetWeekIntent(intent);
        hideWebViewUntilWeekIsReady();
        primeWeekBadge();
        if (forceWeekOpening && webView != null) {
            webView.evaluateJavascript("if(window.reloadSchedule){reloadSchedule();}", value -> injectPersonalizationUi());
        } else {
            applyOpenMode();
            injectPersonalizationUi();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.evaluateJavascript(
                    "if(window.reloadSchedule){reloadSchedule();}",
                    value -> {
                        primeWeekBadge();
                        if (!forceWeekOpening) applyOpenMode();
                        injectPersonalizationUi();
                    }
            );
        }
    }

    @Override
    protected void onDestroy() {
        textRecognizer.close();
        super.onDestroy();
    }

    private boolean isWidgetWeekIntent(Intent intent) {
        return intent != null && "week".equals(intent.getStringExtra("open_mode"));
    }

    private void hideWebViewUntilWeekIsReady() {
        if (webView == null) return;
        webView.setAlpha(0f);
        webView.setVisibility(View.INVISIBLE);
    }

    private void revealWebViewStable() {
        if (webView == null || !pageLoaded) return;
        webView.postDelayed(() -> {
            if (webView == null || !pageLoaded) return;
            webView.setAlpha(1f);
            webView.setVisibility(View.VISIBLE);
        }, 70);
    }

    private void reloadForLanguageUi() {
        if (webView == null) return;
        pageLoaded = false;
        hideWebViewUntilWeekIsReady();
        webView.post(webView::reload);
    }

    /**
     * The widget always opens the week view.  The WebView stays invisible while all the
     * injected UI layers finish their first render, so Today/Edit/Week can never flash
     * successively on screen.  It is revealed only after the week view is the active DOM view.
     */
    private void settleWeekAndReveal() {
        if (webView == null) return;
        final String script = """
                (function(){
                  try{
                    if(typeof setModeFromAndroid==='function')setModeFromAndroid('week');
                    else{
                      if(typeof mode!=='undefined')mode='week';
                      document.querySelectorAll('.view').forEach(function(v){v.classList.remove('active')});
                      var w=document.getElementById('viewWeek');if(w)w.classList.add('active');
                      document.querySelectorAll('.nav').forEach(function(n){n.classList.toggle('active',n.dataset.mode==='week')});
                      if(typeof render==='function')render();
                    }
                    if(window.refreshWeekViewStability)window.refreshWeekViewStability();
                    if(window.refreshFineTuneUi)window.refreshFineTuneUi();
                    var week=document.getElementById('viewWeek');
                    return !!(week&&week.classList.contains('active'));
                  }catch(e){return false}
                })();
                """;
        webView.evaluateJavascript(script, value -> webView.postDelayed(() ->
                webView.evaluateJavascript(script, second -> {
                    primeWeekBadge();
                    if (getIntent() != null) getIntent().removeExtra("open_mode");
                    webView.setAlpha(1f);
                    webView.setVisibility(View.VISIBLE);
                    forceWeekOpening = false;
                }), 45));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_BACKUP) {
            handleBackupResult(resultCode, data);
            return;
        }
        if (requestCode != PICK_TIMETABLE_PHOTO) return;
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            resetPhotoImportButton("Import annulé.");
            return;
        }

        Uri uri = data.getData();
        try {
            final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if (takeFlags != 0) {
                try { getContentResolver().takePersistableUriPermission(uri, takeFlags); }
                catch (Exception ignored) {}
            }
            InputImage image = InputImage.fromFilePath(this, uri);
            textRecognizer.process(image)
                    .addOnSuccessListener(this::sendRecognizedSchedule)
                    .addOnFailureListener(error -> sendOcrError("Impossible de lire cette photo. Essaie une image plus nette et prise bien de face."));
        } catch (Exception e) {
            sendOcrError("Impossible d’ouvrir cette photo.");
        }
    }

    private void pickTimetablePhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_TIMETABLE_PHOTO);
    }

    private void sendRecognizedSchedule(Text result) {
        try {
            JSONObject payload = new JSONObject();
            JSONArray lines = new JSONArray();
            for (Text.TextBlock block : result.getTextBlocks()) {
                for (Text.Line line : block.getLines()) {
                    Rect box = line.getBoundingBox();
                    if (box == null) continue;
                    JSONObject item = new JSONObject();
                    item.put("text", line.getText());
                    item.put("l", box.left);
                    item.put("t", box.top);
                    item.put("r", box.right);
                    item.put("b", box.bottom);
                    lines.put(item);
                }
            }
            payload.put("lines", lines);
            payload.put("fullText", result.getText());
            if (lines.length() == 0) {
                sendOcrError("Aucun texte exploitable n’a été détecté sur cette photo.");
                return;
            }
            if (webView != null) {
                String quoted = JSONObject.quote(payload.toString());
                webView.evaluateJavascript("if(window.applyOcrSchedule){window.applyOcrSchedule(" + quoted + ");}", null);
            }
        } catch (Exception e) {
            sendOcrError("Le texte a été reconnu, mais la conversion a échoué.");
        }
    }

    private void sendOcrError(String message) {
        if (webView == null) return;
        String quoted = JSONObject.quote(message);
        webView.evaluateJavascript("if(window.applyOcrError){window.applyOcrError(" + quoted + ");}", null);
    }

    private void resetPhotoImportButton(String message) {
        if (webView == null) return;
        String quoted = JSONObject.quote(message);
        webView.evaluateJavascript(
                "(function(){var b=document.getElementById('importPhoto');if(b){b.disabled=false;b.textContent='Importer une photo d’emploi du temps';}var s=document.getElementById('importStatus');if(s){s.textContent=" + quoted + ";}})();", null);
    }

    private void clearLegacySyncData() {
        getSharedPreferences("pronote_import_v1", MODE_PRIVATE).edit().clear().apply();
        try {
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.cancel(4101); scheduler.cancel(4102);
        } catch (Exception ignored) {}
        try {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } catch (Exception ignored) {}
    }

    private void primeWeekBadge() {
        if (webView == null || !AdvancedSettingsStore.json(this).optBoolean("singleWeek", false)) return;
        String language = UiSettingsStore.language(this);
        String label = LanguagePackStore.widgetText(this, language, "singleWeek");
        if (label == null) label = "de".equals(language) ? "Einzelwoche" : ("en".equals(language) ? "Single week" : "Semaine unique");
        String quoted = JSONObject.quote(label);
        webView.evaluateJavascript(
                "(function(){try{if(typeof currentWeek!=='undefined')currentWeek='A';if(typeof activeWeek!=='undefined')activeWeek='A';var b=document.getElementById('currentWeekBtn');if(b){b.textContent=" + quoted + ";b.setAttribute('aria-label'," + quoted + ");}var s=document.getElementById('weekTitleLetter');if(s)s.textContent='A';}catch(e){}})();",
                null
        );
    }

    private void applyOpenMode() {
        if (webView == null || getIntent() == null) return;
        String mode = getIntent().getStringExtra("open_mode");
        if ("today".equals(mode) || "week".equals(mode) || "edit".equals(mode)) {
            if ("week".equals(mode)) primeWeekBadge();
            webView.evaluateJavascript("if(window.setModeFromAndroid){setModeFromAndroid('" + mode + "');}", null);
            getIntent().removeExtra("open_mode");
        }
    }

    private void injectPersonalizationUi() {
    if (webView == null) return;
    webView.evaluateJavascript(
            "(function(){if(window.__settingsV3&&!document.getElementById('settingsBtn')){var m=document.getElementById('settingsModal');if(m)m.remove();window.__settingsV3=false;}})();",
            ignored -> webView.evaluateJavascript(UiRuntimeBundle.script(), value -> {
                    primeWeekBadge();
                    if (forceWeekOpening) settleWeekAndReveal();
                    else revealWebViewStable();
            })
    );
}

    private void maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION);
        }
    }

    private void shareBackup() {
        String backup = BackupStore.exportJson(this);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/json");
        share.putExtra(Intent.EXTRA_SUBJECT, "Sauvegarde emploi du temps");
        share.putExtra(Intent.EXTRA_TEXT, backup);
        startActivity(Intent.createChooser(share, "Partager la sauvegarde"));
    }

    private void pickBackup() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, PICK_BACKUP);
    }

    private void handleBackupResult(int resultCode, Intent data) {
        boolean success = false;
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            try { success = BackupStore.importJson(this, readAll(data.getData())); }
            catch (Exception ignored) {}
        }
        if (webView != null) webView.evaluateJavascript("if(window.applyBackupImported){window.applyBackupImported(" + (success ? "true" : "false") + ");}", null);
    }

    private String readAll(Uri uri) throws Exception {
        StringBuilder out = new StringBuilder();
        try (InputStream in = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) out.append(line).append('\n');
        }
        return out.toString();
    }

    private final class ScheduleBridge {
        @JavascriptInterface public String loadSchedule() { return ScheduleStore.exportJson(MainActivity.this); }
        @JavascriptInterface public void saveSchedule(String json) { ScheduleStore.importJson(MainActivity.this, json); ProfileStore.saveCurrent(MainActivity.this); }
        @JavascriptInterface public void pickTimetablePhoto() { runOnUiThread(MainActivity.this::pickTimetablePhoto); }
        @JavascriptInterface public String loadUiSettings() { return UiSettingsStore.exportJson(MainActivity.this); }
        @JavascriptInterface public void saveUiSettings(String json) {
            UiSettingsStore.importJson(MainActivity.this, json);
            runOnUiThread(() -> ScheduleWidgetProvider.refreshAll(MainActivity.this));
        }
        @JavascriptInterface public String loadLanguagePacks() { return LanguagePackStore.listJson(MainActivity.this); }
        @JavascriptInterface public String loadLanguagePack(String code) { return LanguagePackStore.get(MainActivity.this, code); }
        @JavascriptInterface public boolean saveLanguagePack(String json) { return LanguagePackStore.save(MainActivity.this, json); }
        @JavascriptInterface public String downloadLanguageCatalog() { return NativeLanguageDownloader.catalog(); }
        @JavascriptInterface public String downloadLanguagePack(String url) { return NativeLanguageDownloader.downloadAndSave(MainActivity.this, url); }
        @JavascriptInterface public String supportedTranslationLanguages() { return MlLanguagePackGenerator.supportedLanguagesJson(); }
        @JavascriptInterface public void generateLanguagePack(String code, String name) {
            runOnUiThread(() -> MlLanguagePackGenerator.generate(MainActivity.this, code, name, new MlLanguagePackGenerator.Callback() {
                @Override public void onSuccess(String raw) {
                    runOnUiThread(() -> {
                        if (webView == null) return;
                        String quoted = JSONObject.quote(raw);
                        webView.evaluateJavascript("if(window.onGeneratedLanguagePack80){window.onGeneratedLanguagePack80(" + quoted + ");}", null);
                    });
                }
                @Override public void onFailure(String message) {
                    runOnUiThread(() -> {
                        if (webView == null) return;
                        String quoted = JSONObject.quote(message == null ? "Téléchargement impossible" : message);
                        webView.evaluateJavascript("if(window.onGeneratedLanguagePackError80){window.onGeneratedLanguagePackError80(" + quoted + ");}", null);
                    });
                }
            }));
        }
        @JavascriptInterface public void reloadForLanguage() { runOnUiThread(MainActivity.this::reloadForLanguageUi); }
        @JavascriptInterface public String loadWidgetPalette() { return WidgetPaletteStore.getPalette(MainActivity.this); }
        @JavascriptInterface public void saveWidgetPalette(String id) {
            WidgetPaletteStore.setPalette(MainActivity.this, id);
            runOnUiThread(() -> ScheduleWidgetProvider.refreshAll(MainActivity.this));
        }
        @JavascriptInterface public String loadSpecialColors() { return WidgetPaletteStore.specialColorsJson(MainActivity.this); }
        @JavascriptInterface public void saveSpecialColors(String json) {
            WidgetPaletteStore.saveSpecialColorsJson(MainActivity.this, json);
            runOnUiThread(() -> ScheduleWidgetProvider.refreshAll(MainActivity.this));
        }

        @JavascriptInterface public String loadAdvancedSettings() { return AdvancedSettingsStore.exportJson(MainActivity.this); }
        @JavascriptInterface public void saveAdvancedSettings(String json) {
            AdvancedSettingsStore.importJson(MainActivity.this, json);
            if (AdvancedSettingsStore.remindersEnabled(MainActivity.this)) runOnUiThread(MainActivity.this::maybeRequestNotificationPermission);
        }
        @JavascriptInterface public void setCurrentWeek(String letter) { ScheduleStore.setCurrentWeekLetter(MainActivity.this, letter); }

        @JavascriptInterface public String loadEffectiveCourses(String yyyyMmDd) {
            JSONObject root = new JSONObject();
            JSONArray arr = new JSONArray();
            try {
                java.util.Calendar date = java.util.Calendar.getInstance();
                java.util.Date parsed = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(yyyyMmDd == null ? "" : yyyyMmDd);
                if (parsed != null) date.setTime(parsed);
                root.put("dayOff", AdvancedSettingsStore.isDayOff(MainActivity.this, date));
                for (ScheduleData.Course c : ScheduleStore.getCourses(MainActivity.this, date)) {
                    JSONObject o = new JSONObject();
                    o.put("start", c.start); o.put("end", c.end); o.put("label", c.label); o.put("room", c.room); o.put("slot", c.slot); o.put("uncertain", c.uncertain); o.put("color", c.color);
                    arr.put(o);
                }
                root.put("courses", arr);
            } catch (Exception ignored) {}
            return root.toString();
        }

        @JavascriptInterface public String listProfiles() { return ProfileStore.listJson(MainActivity.this); }
        @JavascriptInterface public String createProfile(String name, boolean duplicateCurrent) { return ProfileStore.create(MainActivity.this, name, duplicateCurrent); }
        @JavascriptInterface public String activateProfile(String id) { return ProfileStore.activate(MainActivity.this, id); }
        @JavascriptInterface public void renameProfile(String id, String name) { ProfileStore.rename(MainActivity.this, id, name); }
        @JavascriptInterface public String deleteProfile(String id) { return ProfileStore.delete(MainActivity.this, id); }

        @JavascriptInterface public void shareBackup() { runOnUiThread(MainActivity.this::shareBackup); }
        @JavascriptInterface public void pickBackup() { runOnUiThread(MainActivity.this::pickBackup); }
    }
}

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
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
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
    private final TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clearLegacySyncData();
        ScheduleStore.ensureInitialized(this);
        ProfileStore.ensure(this);
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
                injectBulkUi();
                injectPersonalizationUi();
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
                        injectBulkUi();
                        injectPersonalizationUi();
                        webView.evaluateJavascript("if(window.refreshSettingsV3){refreshSettingsV3();}if(window.refreshAdvancedFeatures){refreshAdvancedFeatures();}if(window.refreshUiPolishSchool){refreshUiPolishSchool();}if(window.refreshCourseColors){refreshCourseColors();}if(window.refreshCoursePaletteV1){refreshCoursePaletteV1();}if(window.refreshLunchBreakUi){refreshLunchBreakUi();}if(window.refreshDoubleLunchUi){refreshDoubleLunchUi();}if(window.refreshBulkCourseUi){refreshBulkCourseUi();}if(window.refreshWeekViewStability){refreshWeekViewStability();}", null);
                    }
            );
        }
    }

    @Override
    protected void onDestroy() {
        textRecognizer.close();
        super.onDestroy();
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

    private void applyOpenMode() {
        if (webView == null || getIntent() == null) return;
        String mode = getIntent().getStringExtra("open_mode");
        if ("today".equals(mode) || "week".equals(mode) || "edit".equals(mode)) {
            webView.evaluateJavascript("if(window.setModeFromAndroid){setModeFromAndroid('" + mode + "');}", null);
            getIntent().removeExtra("open_mode");
        }
    }

    private void injectBulkUi() {
        if (webView == null) return;
        webView.evaluateJavascript(BulkCourseUi.script(), value ->
                webView.evaluateJavascript(WeekViewStabilityUi.script(), null));
    }

    private void injectPersonalizationUi() {
        if (webView == null) return;
        webView.evaluateJavascript(PersonalizationUi2.script(), value ->
                webView.evaluateJavascript(AdvancedFeaturesUi.script(), value2 ->
                        webView.evaluateJavascript(UiPolishAndSchoolCalendarUi.script(), value3 ->
                                webView.evaluateJavascript(CourseColorUi.script(), value4 ->
                                        webView.evaluateJavascript(PaletteSelectorUi.script(), value5 ->
                                                webView.evaluateJavascript(LunchBreakUi.script(), value6 ->
                                                        webView.evaluateJavascript(DoubleLunchUi.script(), value7 ->
                                                                webView.evaluateJavascript(BulkCourseUi.script(), value8 ->
                                                                        webView.evaluateJavascript(WeekViewStabilityUi.script(), null)))))))));
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
        @JavascriptInterface public void saveUiSettings(String json) { UiSettingsStore.importJson(MainActivity.this, json); }

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

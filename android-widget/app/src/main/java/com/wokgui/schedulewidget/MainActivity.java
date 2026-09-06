package com.wokgui.schedulewidget;

import android.app.Activity;
import android.app.job.JobScheduler;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
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

public class MainActivity extends Activity {
    private static final int PICK_TIMETABLE_PHOTO = 5201;

    private WebView webView;
    private final TextRecognizer textRecognizer =
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

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
                    value -> applyOpenMode()
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
        if (requestCode != PICK_TIMETABLE_PHOTO) return;

        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            resetPhotoImportButton("Import annulé.");
            return;
        }

        Uri uri = data.getData();
        try {
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if (takeFlags != 0) {
                try {
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);
                } catch (Exception ignored) {
                }
            }

            InputImage image = InputImage.fromFilePath(this, uri);
            textRecognizer.process(image)
                    .addOnSuccessListener(this::sendRecognizedSchedule)
                    .addOnFailureListener(error -> sendOcrError(
                            "Impossible de lire cette photo. Essaie une image plus nette et prise bien de face."));
        } catch (Exception e) {
            sendOcrError("Impossible d’ouvrir cette photo.");
        }
    }

    private void pickTimetablePhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
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
                webView.evaluateJavascript(
                        "if(window.applyOcrSchedule){window.applyOcrSchedule(" + quoted + ");}",
                        null
                );
            }
        } catch (Exception e) {
            sendOcrError("Le texte a été reconnu, mais la conversion a échoué.");
        }
    }

    private void sendOcrError(String message) {
        if (webView == null) return;
        String quoted = JSONObject.quote(message);
        webView.evaluateJavascript(
                "if(window.applyOcrError){window.applyOcrError(" + quoted + ");}",
                null
        );
    }

    private void resetPhotoImportButton(String message) {
        if (webView == null) return;
        String quoted = JSONObject.quote(message);
        webView.evaluateJavascript(
                "(function(){var b=document.getElementById('importPhoto');"
                        + "if(b){b.disabled=false;b.textContent='Importer une photo d’emploi du temps';}"
                        + "var s=document.getElementById('importStatus');if(s){s.textContent=" + quoted + ";}})();",
                null
        );
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

    private final class ScheduleBridge {
        @JavascriptInterface
        public String loadSchedule() {
            return ScheduleStore.exportJson(MainActivity.this);
        }

        @JavascriptInterface
        public void saveSchedule(String json) {
            ScheduleStore.importJson(MainActivity.this, json);
        }

        @JavascriptInterface
        public void pickTimetablePhoto() {
            runOnUiThread(MainActivity.this::pickTimetablePhoto);
        }
    }
}

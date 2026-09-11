package com.wokgui.schedulewidget;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;

/** Deterministic visual harness for the editable photo-import confirmation screen. */
public class ImportReviewPreviewActivity extends MainActivity {
    private static final String TAG = "EDT_IMPORT_REVIEW";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int readinessAttempts;
    private int openAttempts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler.postDelayed(this::tryOpenPreview, 400);
    }

    private void tryOpenPreview() {
        WebView webView = findViewById(R.id.webView);
        if (webView == null) return;
        webView.evaluateJavascript("typeof window.openTimetableImportReview==='function'&&typeof window.parseOcrSchedule==='function'", ready -> {
            if ("true".equals(ready)) {
                openPreview(webView);
            } else if (++readinessAttempts < 80) {
                handler.postDelayed(this::tryOpenPreview, 250);
            } else {
                failPreview("parser/review functions never became ready");
            }
        });
    }

    private void openPreview(WebView webView) {
        webView.evaluateJavascript(verifiedSampleScript(), result -> {
            if ("\"visible\"".equals(result)) {
                Log.i(TAG, "visible");
                keepPreviewVisible(webView, 350);
                keepPreviewVisible(webView, 900);
                keepPreviewVisible(webView, 1600);
            } else if (++openAttempts < 4) {
                handler.postDelayed(() -> openPreview(webView), 250);
            } else {
                failPreview("review overlay did not become visible: " + result);
            }
        });
    }

    private void keepPreviewVisible(WebView webView, long delayMs) {
        handler.postDelayed(() -> webView.evaluateJavascript(
                "(function(){var e=document.getElementById('edtImportReview');if(e){e.classList.add('show');e.style.display='flex';e.style.zIndex='10000';}})();",
                null), delayMs);
    }

    private void failPreview(String reason) {
        Log.e(TAG, "failed: " + reason);
        throw new IllegalStateException("Import review preview failed: " + reason);
    }

    private String verifiedSampleScript() {
        return "(function(){try{" + sampleScript() +
                "var e=document.getElementById('edtImportReview');" +
                "if(!e||!e.classList.contains('show'))throw new Error('overlay not visible');" +
                "e.style.display='flex';e.style.zIndex='10000';return 'visible';" +
                "}catch(err){console.error('EDT_IMPORT_REVIEW_PREVIEW_ERROR|'+(err&&err.stack?err.stack:String(err)));return 'error';}})();";
    }

    /**
     * Raw ML Kit-like lines deliberately include a missing Wednesday header, three time
     * formats and a multi-line Monday cell. The parser must reconstruct six courses
     * before the review sheet is allowed to open.
     */
    private String sampleScript() {
        return """
                var raw={lines:[
                  {text:'Emploi du temps',l:420,t:40,r:650,b:75},
                  {text:'Lundi',l:205,t:135,r:275,b:165},
                  {text:'Mardi',l:385,t:135,r:455,b:165},
                  {text:'Jeudi',l:745,t:135,r:815,b:165},
                  {text:'Vendredi',l:915,t:135,r:1005,b:165},
                  {text:'08h00 - 09h00',l:35,t:332,r:145,b:368},
                  {text:'09.00 – 10.00',l:35,t:532,r:145,b:568},
                  {text:'10h00 - 11h00',l:35,t:732,r:145,b:768},
                  {text:'5e A',l:205,t:330,r:275,b:360},
                  {text:'Allemand',l:195,t:365,r:290,b:395},
                  {text:'Salle 216',l:200,t:400,r:288,b:430},
                  {text:'3e B · Allemand',l:365,t:530,r:475,b:565},
                  {text:'4e C',l:565,t:330,r:635,b:360},
                  {text:'Allemand',l:555,t:365,r:650,b:395},
                  {text:'5e D · Allemand',l:725,t:730,r:835,b:765},
                  {text:'3e A · Allemand',l:905,t:530,r:1015,b:565},
                  {text:'4e B · Allemand',l:905,t:730,r:1015,b:765}
                ]};
                var parsed=window.parseOcrSchedule(raw);
                if(!parsed||parsed.error)throw new Error('parser failed: '+(parsed&&parsed.error));
                if(parsed.count!==6)throw new Error('expected 6 parsed courses, got '+parsed.count);
                if(!parsed.parsed||!parsed.parsed[4]||parsed.parsed[4].length!==1)throw new Error('missing reconstructed Wednesday column');
                var monday=parsed.parsed[2]&&parsed.parsed[2][0];
                if(!monday||monday.label.indexOf('5e A')<0||monday.label.indexOf('Allemand')<0||monday.room!=='216')throw new Error('multi-line Monday cell was not merged correctly');
                console.log('EDT_IMPORT_PARSE|count='+parsed.count+'|quality='+parsed.quality+'|headers='+(parsed.diagnostics&&parsed.diagnostics.headers));
                window.openTimetableImportReview(parsed);
                """;
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}

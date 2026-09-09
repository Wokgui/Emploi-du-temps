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
        webView.evaluateJavascript("typeof window.openTimetableImportReview==='function'", ready -> {
            if ("true".equals(ready)) {
                openPreview(webView);
            } else if (++readinessAttempts < 80) {
                handler.postDelayed(this::tryOpenPreview, 250);
            } else {
                failPreview("review function never became ready");
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

    private String sampleScript() {
        return "window.openTimetableImportReview({count:6,days:[2,3,4,5,6],parsed:{" +
                "2:[{start:'08:00',end:'09:00',label:'5e A · Allemand',room:'216'},{start:'10:00',end:'11:00',label:'3e B · Allemand',room:'215'}]," +
                "3:[{start:'09:00',end:'10:00',label:'4e C · Allemand',room:'217'}]," +
                "4:[{start:'11:00',end:'12:00',label:'5e D · Allemand',room:'216'}]," +
                "5:[{start:'13:00',end:'14:00',label:'3e A · Allemand',room:'215'}]," +
                "6:[{start:'15:00',end:'16:00',label:'4e B · Allemand',room:'217'}]" +
                "}});";
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}

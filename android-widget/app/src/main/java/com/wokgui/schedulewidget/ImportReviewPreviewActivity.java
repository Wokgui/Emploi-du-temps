package com.wokgui.schedulewidget;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;

/** Deterministic visual harness for the editable photo-import confirmation screen. */
public class ImportReviewPreviewActivity extends MainActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int attempts;

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
                webView.evaluateJavascript(sampleScript(), null);
            } else if (++attempts < 80) {
                handler.postDelayed(this::tryOpenPreview, 250);
            }
        });
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

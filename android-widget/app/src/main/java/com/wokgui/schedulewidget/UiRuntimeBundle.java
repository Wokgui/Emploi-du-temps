package com.wokgui.schedulewidget;

/** Single WebView injection entry point. Runtime behavior is grouped by responsibility. */
final class UiRuntimeBundle {
    static final String CHUNK_MARKER = "\n/*__EDT_UI_CHUNK__*/\n";

    private UiRuntimeBundle() {}

    /**
     * Returns the complete runtime while preserving explicit boundaries between the
     * legacy UI layers. A normal WebView can still execute this as one script because
     * the boundaries are comments. ResilientWebView recognizes them and evaluates one
     * layer per animation frame, keeping the already-loaded timetable paintable during
     * cold starts.
     */
    static String script() {
        return String.join(CHUNK_MARKER, ChunkedUiScripts.all());
    }

    /**
     * The OCR parser and editable import sheet are large and irrelevant to normal app
     * navigation. Schedule them only once the main UI has finished loading and Chromium
     * reports idle time. An OCR result arriving early is queued by LazyImportBootstrapUi.
     */
    static String idleImportScript() {
        String body = prepareChunk(ImportParserUi.script()) + "\n" + prepareChunk(ImportReviewUi.script());
        return """
                (function(){
                  try{
                    if(window.__edtLazyImportScheduled)return;
                    window.__edtLazyImportScheduled=true;
                    var run=function(){
                      try{
                        var pending=window.__edtPendingOcrRaw;
                        %s
                        window.__edtLazyImportReady=(typeof window.parseOcrSchedule==='function'&&typeof window.openTimetableImportReview==='function');
                        if(window.__edtLazyImportReady&&pending!=null){
                          window.__edtPendingOcrRaw=null;
                          window.applyOcrSchedule(pending);
                        }
                        console.log('EDT_LAZY_IMPORT|ready='+window.__edtLazyImportReady);
                      }catch(e){console.error('EDT_LAZY_IMPORT|error='+e)}
                    };
                    if(typeof requestIdleCallback==='function')requestIdleCallback(run,{timeout:8000});
                    else setTimeout(run,1800);
                  }catch(e){console.log('EDT_LAZY_IMPORT',e)}
                })();
                """.formatted(body);
    }

    /** Several generations of UI polish legitimately move the same controls. */
    static String domSafetyPrelude() {
        return """
                (function(){
                  try{
                    if(window.__edtSafeInsertBefore)return;
                    window.__edtSafeInsertBefore=true;
                    var nativeInsert=Node.prototype.insertBefore;
                    Node.prototype.insertBefore=function(newNode,referenceNode){
                      if(referenceNode&&referenceNode.parentNode!==this)referenceNode=null;
                      return nativeInsert.call(this,newNode,referenceNode);
                    };
                  }catch(e){}
                })();
                """;
    }

    /** Applies compatibility repairs and aligns version labels to every independent layer. */
    static String prepareChunk(String script) {
        script = repairGeneratedJavaScript(script);
        return script.replace("APP_VERSION='6.31'", "APP_VERSION='6.43'")
                     .replace("APP_VERSION='6.32'", "APP_VERSION='6.43'")
                     .replace("APP_VERSION='6.33'", "APP_VERSION='6.43'")
                     .replace("APP_VERSION='6.34'", "APP_VERSION='6.43'")
                     .replace("APP_VERSION='6.35'", "APP_VERSION='6.43'")
                     .replace("APP_VERSION='6.36'", "APP_VERSION='6.43'")
                     .replace("APP_VERSION='6.37'", "APP_VERSION='6.43'")
                     .replace("APP_VERSION='6.38'", "APP_VERSION='6.43'")
                     .replace("APP_VERSION='6.39'", "APP_VERSION='6.43'")
                     .replace("APP_VERSION='6.40'", "APP_VERSION='6.43'")
                     .replace("APP_VERSION='6.41'", "APP_VERSION='6.43'")
                     .replace("APP_VERSION='6.42'", "APP_VERSION='6.43'");
    }

    /**
     * Repairs escapes and legacy DOM assumptions before the scripts reach the
     * WebView. Keeping compatibility repairs in one place lets the old UI layers
     * be progressively consolidated without shipping invalid JavaScript.
     *
     * 6.43 also removes redundant long-session background work. Three generations
     * of the week painter used to keep independent timers alive, while several
     * observers watched whole subtrees even though the render functions already
     * request the same refreshes. The newest week painter remains authoritative.
     */
    private static String repairGeneratedJavaScript(String script) {
        script = script.replace("l'application", "l\\'application");
        script = script.replace(
                "catch(e){return 'fr'}\n    function tr(fr,en,de)",
                "catch(e){return 'fr'}}\n    function tr(fr,en,de)");
        script = script.replace(
                ".length).join('\n');",
                ".length).join('\\n');");
        script = script.replace(
                "activeWeek+'.\n\n'+summary+'\n\nRemplacer",
                "activeWeek+'.\\n\\n'+summary+'\\n\\nRemplacer");
        script = script.replace(
                "if(importBtn&&bar.nextElementSibling!==importBtn)view.insertBefore(bar,importBtn);",
                "if(importBtn&&importBtn.parentNode===view&&bar.nextElementSibling!==importBtn)view.insertBefore(bar,importBtn);");
        script = script.replace(
                "if(theme&&preview&&preview.nextElementSibling!==theme)sheet.insertBefore(preview,theme);",
                "if(theme&&preview&&theme.parentNode===sheet&&preview.parentNode===sheet&&preview.nextElementSibling!==theme)sheet.insertBefore(preview,theme);");
        script = script.replace(
                "if(languageBox&&appBox&&languageBox.nextElementSibling!==appBox)sheet.insertBefore(languageBox,appBox);",
                "if(languageBox&&appBox&&languageBox.parentNode===sheet&&appBox.parentNode===sheet&&languageBox.nextElementSibling!==appBox)sheet.insertBefore(languageBox,appBox);");

        // FinalPolish and Stability69 are older week painters. Keeping their timers
        // alive alongside Stability70 causes needless DOM scans for the whole session.
        script = script.replace(
                "setInterval(paintWeek,15000);",
                "/* EDT 6.43: Stability70 owns periodic week repainting. */");
        script = script.replace(
                "setInterval(()=>{if(typeof mode!=='undefined'&&mode==='week')paintWeek69()},30000);",
                "/* EDT 6.43: legacy Stability69 periodic repaint removed. */");
        script = script.replace(
                "setInterval(()=>{if(typeof mode!=='undefined'&&mode==='week')paintWeek70()},30000);",
                "if(!window.__edtWeekClockTimer){window.__edtWeekClockTimer=setInterval(()=>{if(!document.hidden&&typeof mode!=='undefined'&&mode==='week')paintWeek70()},60000)}");

        // These roots are rebuilt at their first level. Watching every descendant
        // multiplies callbacks when a render adds several nested labels and controls.
        script = script.replace(
                "['weekGrid','todayList','editList'].forEach(id=>{const el=document.getElementById(id);if(el)new MutationObserver(scheduleLiteral84).observe(el,{childList:true,subtree:true})});",
                "['weekGrid','todayList','editList'].forEach(id=>{const el=document.getElementById(id);if(el)new MutationObserver(scheduleLiteral84).observe(el,{childList:true,subtree:false})});");

        // Stability71 already wraps the render/refresh entry points, so descendant
        // mutations do not need to recursively schedule another refresh. Context is
        // included explicitly so narrowing the observers does not lose that update.
        script = script.replace(
                "['renderEdit','refreshSettingsV3','refreshAdvancedFeatures','refreshFineTuneUi','refreshStability70','refreshWeekendUi'].forEach(wrap);",
                "['renderContext','renderEdit','refreshSettingsV3','refreshAdvancedFeatures','refreshFineTuneUi','refreshStability70','refreshWeekendUi'].forEach(wrap);");
        script = script.replace(
                "new MutationObserver(()=>scheduleRefresh()).observe(root,{childList:true,subtree:true});",
                "new MutationObserver(()=>scheduleRefresh()).observe(root,{childList:true,subtree:false});");

        // SettingsLayout only needs to notice top-level setting sections being added.
        // Nested changes are already handled by explicit settings refresh functions.
        script = script.replace(
                "new MutationObserver(schedule).observe(sheet,{childList:true,subtree:true});",
                "new MutationObserver(schedule).observe(sheet,{childList:true,subtree:false});");
        return script;
    }
}

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
        return script.replace("APP_VERSION='6.31'", "APP_VERSION='6.45'")
                     .replace("APP_VERSION='6.32'", "APP_VERSION='6.45'")
                     .replace("APP_VERSION='6.33'", "APP_VERSION='6.45'")
                     .replace("APP_VERSION='6.34'", "APP_VERSION='6.45'")
                     .replace("APP_VERSION='6.35'", "APP_VERSION='6.45'")
                     .replace("APP_VERSION='6.36'", "APP_VERSION='6.45'")
                     .replace("APP_VERSION='6.37'", "APP_VERSION='6.45'")
                     .replace("APP_VERSION='6.38'", "APP_VERSION='6.45'")
                     .replace("APP_VERSION='6.39'", "APP_VERSION='6.45'")
                     .replace("APP_VERSION='6.40'", "APP_VERSION='6.45'")
                     .replace("APP_VERSION='6.41'", "APP_VERSION='6.45'")
                     .replace("APP_VERSION='6.42'", "APP_VERSION='6.45'")
                     .replace("APP_VERSION='6.43'", "APP_VERSION='6.45'")
                     .replace("APP_VERSION='6.44'", "APP_VERSION='6.45'");
    }

    /**
     * Repairs escapes and legacy DOM assumptions before the scripts reach the WebView.
     * 6.44 stopped refresh-time wrapper accumulation. 6.45 additionally coalesces the
     * remaining whole-view mutation work so repeated tab changes cannot build a queue of
     * palette and break-decoration passes behind the user's taps.
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

        // Keep one authoritative minute clock instead of four independent legacy timers.
        script = script.replace(
                "setInterval(paintWeek,15000);",
                "/* EDT 6.44: legacy FinalPolish clock removed. */");
        script = script.replace(
                "setInterval(()=>{if(typeof mode!=='undefined'&&mode==='week')paintWeek69()},30000);",
                "/* EDT 6.44: legacy Stability69 clock removed. */");
        script = script.replace(
                "setInterval(()=>{if(typeof mode!=='undefined'&&mode==='week')paintWeek70()},30000);",
                "if(!window.__edtWeekClockTimer){window.__edtWeekClockTimer=setInterval(()=>{if(!document.hidden&&typeof mode!=='undefined'&&mode==='week')paintWeek70()},60000)}");
        script = script.replace(
                "setInterval(updateNowMarkers,60000);",
                "/* EDT 6.44: Stability70 owns the minute clock. */");
        script = script.replace(
                "setInterval(()=>setTimeout(polishWeekNowMarker,45),60000);",
                "/* EDT 6.44: Stability70 owns the minute clock. */");
        script = script.replace(
                "setInterval(()=>{if(window.paintWeek69)window.paintWeek69()},30000);",
                "/* EDT 6.44: legacy WeekGeometry clock removed. */");

        // FineTune: install its form submit wrapper once. Calling refreshFineTuneUi no
        // longer adds the same wrapper again after later form wrappers become outermost.
        script = script.replace(
                "function refresh(){\n                  applySpecialCss();renderSpecialControls();ensureFullCoursePicker();wrapCourseSubmit();bindSyncToggle();repaintLiteralCourses();\n                }\n                window.refreshFineTuneUi=refresh;",
                "function refresh(){\n                  applySpecialCss();renderSpecialControls();ensureFullCoursePicker();bindSyncToggle();repaintLiteralCourses();\n                }\n                wrapCourseSubmit();window.refreshFineTuneUi=refresh;");

        // Stability69: its renderWeek hook is structural and must be installed once,
        // not every time another stability layer asks it to refresh.
        script = script.replace(
                "function refresh(){ensureCycleChoices();syncCycleDom(currentMode());wrapWeekRender();paintWeek69();setVersion()}\n                window.refreshStability69=refresh;",
                "function refresh(){ensureCycleChoices();syncCycleDom(currentMode());paintWeek69();setVersion()}\n                wrapWeekRender();window.refreshStability69=refresh;");

        // Stability70 was the largest multiplier: refresh() used to revisit five hooks.
        script = script.replace(
                "function refresh(){installWrappers();syncCycleUi();polishSettings();applyBreakVisibility();paintWeek70()}\n                window.refreshStability70=refresh;",
                "function refresh(){syncCycleUi();polishSettings();applyBreakVisibility();paintWeek70()}\n                installWrappers();window.refreshStability70=refresh;");

        // Lunch/badge renderer: initial override is required, but doing it from every
        // refresh discards later wrappers and starts a new wrapping cycle.
        script = script.replace(
                "try{\n                    bindOverrides();wireBreakSettings();ensureCourseBadgeField();wrapCourseSubmit();\n                    syncBreakCells();fitBreakLabels();decorateCourseBadges();if(window.refreshCourseColors)window.refreshCourseColors();polishWeekNowMarker();",
                "try{\n                    wireBreakSettings();ensureCourseBadgeField();\n                    syncBreakCells();fitBreakLabels();decorateCourseBadges();if(window.refreshCourseColors)window.refreshCourseColors();polishWeekNowMarker();");
        script = script.replace(
                "if(modal)new MutationObserver(()=>{if(modal.classList.contains('show'))setTimeout(()=>{syncCourseBadgeField();wrapCourseSubmit()},0)}).observe(modal,{attributes:true,attributeFilter:['class']});",
                "if(modal)new MutationObserver(()=>{if(modal.classList.contains('show'))setTimeout(()=>{syncCourseBadgeField()},0)}).observe(modal,{attributes:true,attributeFilter:['class']});");

        // Widget-label submit hook is also permanent once installed. Modal openings only
        // need to refill the field, not wrap onsubmit again.
        script = script.replace(
                "new MutationObserver(()=>{if(modal.classList.contains('show'))setTimeout(()=>{fillCourseWidgetField();wrapCourseSubmit()},0)}).observe(modal,{attributes:true,attributeFilter:['class']});",
                "new MutationObserver(()=>{if(modal.classList.contains('show'))setTimeout(()=>{fillCourseWidgetField()},0)}).observe(modal,{attributes:true,attributeFilter:['class']});");

        // Weekend wrappers execute after all legacy render overrides have been loaded.
        // refreshWeekendUi itself becomes data/DOM-only, so it cannot grow call chains.
        script = script.replace(
                "wrapTodayKey();wrapExport();wrapDayTabs();wrapRenderEdit();wrapRenderWeek();wrapSettingsRefresh();\n                    decorateDayTabs();updateRemoveButton();updateWeekGridGeometry();",
                "decorateDayTabs();updateRemoveButton();updateWeekGridGeometry();");
        script = script.replace(
                "window.refreshWeekendUi=refresh;\n                refresh();",
                "window.refreshWeekendUi=refresh;\n                refresh();\n                window.__edtInstallWeekendWrappers=function(){if(window.__edtWeekendWrappersInstalled)return;window.__edtWeekendWrappersInstalled=true;wrapTodayKey();wrapExport();wrapDayTabs();wrapRenderEdit();wrapRenderWeek();wrapSettingsRefresh()};");

        // Root-level mutation is enough for lists that are recreated by render functions.
        script = script.replace(
                "['weekGrid','todayList','editList'].forEach(id=>{const el=document.getElementById(id);if(el)new MutationObserver(scheduleLiteral84).observe(el,{childList:true,subtree:true})});",
                "['weekGrid','todayList','editList'].forEach(id=>{const el=document.getElementById(id);if(el)new MutationObserver(scheduleLiteral84).observe(el,{childList:true,subtree:false})});");

        // 6.45: one render can add dozens of children. Coalesce all break/badge work into
        // a single pass rather than queueing a new zero-delay timeout for each mutation.
        script = script.replace(
                "for(const id of ['todayList','weekGrid','editList']){const el=document.getElementById(id);if(el)new MutationObserver(()=>setTimeout(()=>{syncBreakCells();decorateCourseBadges();fitBreakLabels()},0)).observe(el,{childList:true,subtree:true})}",
                "let __edtBreakDecorTimer=0;function __edtQueueBreakDecor(){if(__edtBreakDecorTimer)return;__edtBreakDecorTimer=setTimeout(()=>{__edtBreakDecorTimer=0;syncBreakCells();decorateCourseBadges();fitBreakLabels()},20)}for(const id of ['todayList','weekGrid','editList']){const el=document.getElementById(id);if(el)new MutationObserver(__edtQueueBreakDecor).observe(el,{childList:true,subtree:false})}");

        // 6.45: PaletteSelector used to schedule a full app/settings repaint for every
        // subtree mutation. Rebuilding a timetable could therefore create a long tail of
        // redundant full-tree scans. Keep at most one pending repaint.
        script = script.replace(
                "['weekGrid','todayList','editList','courseColorPalette'].forEach(id=>{const el=document.getElementById(id);if(el)new MutationObserver(()=>setTimeout(repaint,0)).observe(el,{childList:true,subtree:true})});",
                "let __edtPaletteRepaintTimer=0;function __edtQueuePaletteRepaint(){if(__edtPaletteRepaintTimer)return;__edtPaletteRepaintTimer=setTimeout(()=>{__edtPaletteRepaintTimer=0;repaint()},24)}['weekGrid','todayList','editList','courseColorPalette'].forEach(id=>{const el=document.getElementById(id);if(el)new MutationObserver(__edtQueuePaletteRepaint).observe(el,{childList:true,subtree:false})});");

        // Stability71 already wraps render/refresh entry points; descendant mutations do
        // not need recursively to schedule the same work.
        script = script.replace(
                "['renderEdit','refreshSettingsV3','refreshAdvancedFeatures','refreshFineTuneUi','refreshStability70','refreshWeekendUi'].forEach(wrap);",
                "['renderContext','renderEdit','refreshSettingsV3','refreshAdvancedFeatures','refreshFineTuneUi','refreshStability70','refreshWeekendUi'].forEach(wrap);");
        script = script.replace(
                "new MutationObserver(()=>scheduleRefresh()).observe(root,{childList:true,subtree:true});",
                "new MutationObserver(()=>scheduleRefresh()).observe(root,{childList:true,subtree:false});");

        // SettingsLayout only needs top-level setting sections being added.
        script = script.replace(
                "new MutationObserver(schedule).observe(sheet,{childList:true,subtree:true});",
                "new MutationObserver(schedule).observe(sheet,{childList:true,subtree:false});");

        // When school-holiday integration is disabled, do not show the explanatory
        // sentence requested for removal. The enabled state keeps its useful count.
        script = script.replace(
                "):tx('Active cette option pour que l’application et le widget ignorent automatiquement les vacances scolaires.','Enable this so the app and widget automatically skip school holidays.','Aktivieren, damit App und Widget Schulferien automatisch überspringen.')}\n",
                "):''}\n");
        return script;
    }
}

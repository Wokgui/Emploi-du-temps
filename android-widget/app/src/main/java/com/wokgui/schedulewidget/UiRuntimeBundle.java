package com.wokgui.schedulewidget;

/** Single WebView injection entry point. Runtime behavior is grouped by responsibility. */
final class UiRuntimeBundle {
    private UiRuntimeBundle() {}

    static String script() {
        StringBuilder out = new StringBuilder(460 * 1024);
        out.append(BaseSettingsUi.script()).append('\n');
        out.append(TimetableCoreUi.script()).append('\n');
        out.append(WeekViewStabilityUi.script()).append('\n');
        out.append(ScheduleDisplayUi.script()).append('\n');
        out.append(LocalizationUi.script()).append('\n');
        out.append(ImportParserUi.script()).append('\n');
        out.append(LocalizationFinalUi.script()).append('\n');
        out.append(WorkflowUi.script()).append('\n');
        out.append(TemporalStateUi.script()).append('\n');

        String script = repairGeneratedJavaScript(out.toString());

        // Keep all runtime version labels aligned without duplicating edits across
        // the consolidated UI modules.
        script = script.replace("APP_VERSION='6.31'", "APP_VERSION='6.35'")
                       .replace("APP_VERSION='6.32'", "APP_VERSION='6.35'")
                       .replace("APP_VERSION='6.33'", "APP_VERSION='6.35'")
                       .replace("APP_VERSION='6.34'", "APP_VERSION='6.35'");
        return script;
    }

    /**
     * Repairs escapes and legacy DOM assumptions before the scripts reach the
     * WebView. Keeping compatibility repairs in one place lets the old UI layers
     * be progressively consolidated without shipping invalid JavaScript.
     */
    private static String repairGeneratedJavaScript(String script) {
        // Apostrophe inside a single-quoted JavaScript/HTML literal.
        script = script.replace("l'application", "l\\'application");

        // Legacy Stability69 layer was missing the closing brace of lang().
        script = script.replace(
                "catch(e){return 'fr'}\n    function tr(fr,en,de)",
                "catch(e){return 'fr'}}\n    function tr(fr,en,de)");

        // OcrImport80 used Java \n escapes inside JavaScript single-quoted strings;
        // text-block processing turned them into raw line breaks, which JS rejects.
        script = script.replace(
                ".length).join('\n');",
                ".length).join('\\n');");
        script = script.replace(
                "activeWeek+'.\n\n'+summary+'\n\nRemplacer",
                "activeWeek+'.\\n\\n'+summary+'\\n\\nRemplacer");

        // Some older layers assume #importPhoto is still a direct child of
        // #viewEdit. Later layers can wrap or move it, making insertBefore throw
        // NotFoundError on cold starts. Only reorder when the reference node
        // really belongs to the intended parent.
        script = script.replace(
                "if(importBtn&&bar.nextElementSibling!==importBtn)view.insertBefore(bar,importBtn);",
                "if(importBtn&&importBtn.parentNode===view&&bar.nextElementSibling!==importBtn)view.insertBefore(bar,importBtn);");

        return script;
    }
}

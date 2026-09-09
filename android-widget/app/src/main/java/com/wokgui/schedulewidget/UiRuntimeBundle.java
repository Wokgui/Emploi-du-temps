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

        // Java consumes \' inside text blocks. Restore the escape required by the
        // single-quoted JavaScript HTML literal before evaluating the bundle.
        String script = out.toString().replace("l'application", "l\\'application");

        // Keep all runtime version labels aligned without duplicating edits across
        // the consolidated UI modules.
        script = script.replace("APP_VERSION='6.31'", "APP_VERSION='6.33'")
                       .replace("APP_VERSION='6.32'", "APP_VERSION='6.33'");
        return script;
    }
}

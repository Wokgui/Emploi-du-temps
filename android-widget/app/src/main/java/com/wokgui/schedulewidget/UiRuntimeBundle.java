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
        return out.toString();
    }
}

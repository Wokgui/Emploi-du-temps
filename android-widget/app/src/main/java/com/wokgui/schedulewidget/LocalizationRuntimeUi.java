package com.wokgui.schedulewidget;

/** Ordered localization runtime, isolated from timetable rendering. */
final class LocalizationRuntimeUi {
    private LocalizationRuntimeUi() {}

    static String script() {
        StringBuilder out = new StringBuilder(110 * 1024);
        out.append(Localization75Ui.script()).append('\n');
        out.append(LayoutLanguage77Ui.script()).append('\n');
        out.append(Stability78Ui.script()).append('\n');
        out.append(Stability79Ui.script()).append('\n');
        out.append(Stability80Ui.script()).append('\n');
        out.append(Stability81Ui.script()).append('\n');
        return out.toString();
    }
}

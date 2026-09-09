package com.wokgui.schedulewidget;

/** Single ordered WebView injection entry point. The runtime is split into semantic modules
 * so feature work no longer grows the root injection chain. */
final class UiRuntimeBundle {
    private UiRuntimeBundle() {}

    static String script() {
        StringBuilder out = new StringBuilder(420 * 1024);
        out.append(TimetableRuntimeUi.script()).append('\n');
        out.append(LocalizationRuntimeUi.script()).append('\n');
        out.append(FeatureRuntimeUi.script()).append('\n');
        return out.toString();
    }
}

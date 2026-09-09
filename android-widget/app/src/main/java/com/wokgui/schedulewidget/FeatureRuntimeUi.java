package com.wokgui.schedulewidget;

/** Feature runtime for OCR import, workflow helpers and settings extensions. */
final class FeatureRuntimeUi {
    private FeatureRuntimeUi() {}

    static String script() {
        StringBuilder out = new StringBuilder(150 * 1024);
        out.append(OcrImport80Ui.script()).append('\n');
        out.append(Workflow85Ui.script()).append('\n');
        out.append(SettingsLayoutUi.script()).append('\n');
        out.append(EditHistoryUi.script()).append('\n');
        out.append(OcrPreviewUi.script()).append('\n');
        out.append(SettingsResetUi.script()).append('\n');
        return out.toString();
    }
}

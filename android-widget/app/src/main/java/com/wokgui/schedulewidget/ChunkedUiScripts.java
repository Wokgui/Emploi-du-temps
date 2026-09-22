package com.wokgui.schedulewidget;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** Builds the runtime UI as small independent JavaScript units instead of one huge eval. */
final class ChunkedUiScripts {
    private ChunkedUiScripts() {}

    static String[] all() {
        List<String> out = new ArrayList<>(60);
        add(out, HeavyPanelUi648.prelude());
        if (BuildConfig.DEBUG) add(out, HeavyPanelPerformanceUi648.prelude());
        add(out, UiRuntimeBundle.domSafetyPrelude());
        addLayers(out, BaseSettingsUi.class, 1);
        add(out, FastInteractionUi.script());
        add(out, LazyImportBootstrapUi.script());
        /* 7.42: Advanced reminders (layer 3) and school holidays (layer 4)
         * must be created in the same WebView frame. ResilientWebView evaluates one
         * UI chunk per animation frame, so keeping them as two chunks caused the
         * visible "Rappels first, Vacances one frame later" effect. */
        addLayers(out, TimetableCoreUi.class, 3);
        add(out, invokeLayer(TimetableCoreUi.class, 3) + "\n" + invokeLayer(TimetableCoreUi.class, 4));
        for (int i = 5; i < 10; i++) add(out, invokeLayer(TimetableCoreUi.class, i));
        add(out, WeekViewStabilityUi.script());
        addScheduleDisplayLayers(out);
        addLayers(out, LocalizationUi.class, 5);
        addLayers(out, LocalizationFinalUi.class, 1);
        addLayers(out, WorkflowUi.class, 5);
        add(out, TemporalStateUi.script());
        add(out, StartupViewRecoveryUi.script());
        add(out, LunchIconCleanupUi.script());
        add(out, Polish644Ui.script());
        add(out, NavigationPerformanceUi.script());
        add(out, InstantViewUi647.script());
        add(out, HeavyPanelUi648.script());
        add(out, HeavyPanelExposureUi652.script());
        add(out, RenderPipelineUi650.script());
        add(out, RenderBurstUi650.script());
        add(out, ActionChainUi651.script());
        add(out, SettingsLunchPolish653Ui.script());
        add(out, LunchBandContinuity656Ui.script());
        // The legacy-named layer remains the final week appearance owner through 6.65.
        add(out, WeekAppearance658Ui.script());
        add(out, Feedback660Ui.script());
        add(out, Feedback661Ui.script());
        add(out, Feedback662Ui.script());
        add(out, Feedback663Ui.script());
        add(out, Feedback664Ui.script());
        add(out, Feedback665Ui.script());
        add(out, Feedback666Ui.script());
        add(out, Feedback672Ui.script());
        add(out, Feedback673Ui.script());
        add(out, Feedback674Ui.script());
        add(out, Feedback675Ui.script());
        add(out, Feedback676Ui.script());
        add(out, Feedback678Ui.script());
        add(out, WeekView744Ui.script());
        add(out, Settings745Ui.script());
        add(out, Settings746Ui.script());
        add(out, WeekView746Ui.script());
        add(out, Layout747Ui.script());
        if (BuildConfig.DEBUG) add(out, HeavyPanelPerformanceUi648.script());
        add(out, UiRuntimeBundle.idleImportScript());
        return out.toArray(new String[0]);
    }

    private static void addScheduleDisplayLayers(List<String> out) {
        for (int i = 0; i < 8; i++) {
            String script = invokeLayer(ScheduleDisplayUi.class, i);
            if (i == 0) script = RuntimeRepair644.repairScheduleDisplayLayer0(script);
            add(out, script);
        }
    }

    private static void addLayers(List<String> out, Class<?> type, int count) {
        for (int i = 0; i < count; i++) add(out, invokeLayer(type, i));
    }

    private static String invokeLayer(Class<?> type, int index) {
        try {
            Method method = type.getDeclaredMethod("layer" + index);
            method.setAccessible(true);
            Object value = method.invoke(null);
            return value instanceof String ? (String) value : "";
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load UI layer " + type.getSimpleName() + ".layer" + index, e);
        }
    }

    private static void add(List<String> out, String script) {
        if (script == null || script.trim().isEmpty()) return;
        script = LegacyChainRepair651.repair(UiRuntimeBundle.prepareChunk(script));
        out.add(script.replace("APP_VERSION='6.45'", "APP_VERSION='6.65'")
                      .replace("APP_VERSION='6.55'", "APP_VERSION='6.65'")
                      .replace("APP_VERSION='6.56'", "APP_VERSION='6.65'")
                      .replace("APP_VERSION='6.58'", "APP_VERSION='6.65'")
                      .replace("APP_VERSION='6.59'", "APP_VERSION='6.65'")
                      .replace("APP_VERSION='6.60'", "APP_VERSION='6.65'")
                      .replace("APP_VERSION='6.61'", "APP_VERSION='6.65'")
                      .replace("APP_VERSION='6.62'", "APP_VERSION='6.65'")
                      .replace("APP_VERSION='6.63'", "APP_VERSION='6.65'")
                      .replace("APP_VERSION='6.64'", "APP_VERSION='6.65'")
                      .replace("APP_VERSION='6.65'", "APP_VERSION='6.66'")
                      .replace("APP_VERSION='6.66'", "APP_VERSION='6.67'")
                      .replace("APP_VERSION='6.67'", "APP_VERSION='6.68'")
                      .replace("APP_VERSION='6.68'", "APP_VERSION='6.69'")
                      .replace("APP_VERSION='6.69'", "APP_VERSION='6.71'")
                      .replace("APP_VERSION='6.70'", "APP_VERSION='6.71'")
                      .replace("APP_VERSION='6.71'", "APP_VERSION='6.72'")
                      .replace("APP_VERSION='6.72'", "APP_VERSION='6.73'")
                      .replace("APP_VERSION='6.73'", "APP_VERSION='6.74'")
                      .replace("APP_VERSION='6.74'", "APP_VERSION='6.75'")
                      .replace("APP_VERSION='6.75'", "APP_VERSION='6.76'")
                      .replace("APP_VERSION='6.76'", "APP_VERSION='6.77'")
                      .replace("APP_VERSION='6.77'", "APP_VERSION='6.78'"));
    }
}

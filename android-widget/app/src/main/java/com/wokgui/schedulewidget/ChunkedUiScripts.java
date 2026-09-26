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
        addLayers(out, TimetableCoreUi.class, 10);
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
        add(out, Feedback672Ui.script());
        add(out, Feedback678Ui.script());
        add(out, CalendarNavigation757Ui.script());
        add(out, Feedback764Ui.script());
        add(out, Feedback765Ui.script());
        add(out, Feedback766Ui.script());
        add(out, Feedback767Ui.script());
        add(out, Feedback768Ui.script());
        add(out, Feedback769Ui.script());
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
        script = script.replace("Farbenblind-Palette", "Daltonismus")
                       .replace("Color-blind palette", "Colour-blind");
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
                      .replace("APP_VERSION='6.77'", "APP_VERSION='6.78'")
                      .replace("APP_VERSION='6.78'", "APP_VERSION='6.79'")
                      .replace("APP_VERSION='6.79'", "APP_VERSION='7.59'")
                      .replace("APP_VERSION='7.59'", "APP_VERSION='7.60'")
                      .replace("APP_VERSION='7.60'", "APP_VERSION='7.62'")
                      .replace("APP_VERSION='7.62'", "APP_VERSION='7.63'")
                      .replace("APP_VERSION='7.63'", "APP_VERSION='7.64'")
                      .replace("APP_VERSION='7.64'", "APP_VERSION='7.65'")
                      .replace("APP_VERSION='7.65'", "APP_VERSION='7.66'")
                      .replace("APP_VERSION='7.66'", "APP_VERSION='7.67'")
                      .replace("APP_VERSION='7.67'", "APP_VERSION='7.68'")
                      .replace("APP_VERSION='7.68'", "APP_VERSION='7.69'")
                      .replace("APP_VERSION='7.69'", "APP_VERSION='7.70'")
                      .replace("APP_VERSION='7.70'", "APP_VERSION='7.72'"));
    }
}

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
        // 6.58 owns the final week colours and per-day lunch position.
        add(out, WeekAppearance658Ui.script());
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
        out.add(script.replace("APP_VERSION='6.45'", "APP_VERSION='6.58'")
                      .replace("APP_VERSION='6.55'", "APP_VERSION='6.58'")
                      .replace("APP_VERSION='6.56'", "APP_VERSION='6.58'"));
    }
}

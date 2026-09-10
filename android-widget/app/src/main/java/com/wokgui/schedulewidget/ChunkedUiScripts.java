package com.wokgui.schedulewidget;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** Builds the runtime UI as small independent JavaScript units instead of one huge eval. */
final class ChunkedUiScripts {
    private ChunkedUiScripts() {}

    static String[] all() {
        List<String> out = new ArrayList<>(45);
        add(out, UiRuntimeBundle.domSafetyPrelude());
        addLayers(out, BaseSettingsUi.class, 1);
        // Install paint-first tap handling before the expensive compatibility layers.
        add(out, FastInteractionUi.script());
        // Prevent the old inline OCR handler from applying data before review UI is ready.
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
        // Pure-CSS final polish plus the final one-time weekend hook installation.
        add(out, Polish644Ui.script());
        // Later layers rebuild controls; refresh the fast handlers once at the end.
        add(out, FastInteractionUi.script());
        // Record function identity/DOM size during real sessions. No production behavior.
        add(out, RuntimeDiagnostics644Ui.script());
        // OCR parsing/review no longer blocks normal startup or navigation.
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
        out.add(UiRuntimeBundle.prepareChunk(script));
    }
}
package com.wokgui.schedulewidget;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** Builds the runtime UI as small independent JavaScript units instead of one huge eval. */
final class ChunkedUiScripts {
    private ChunkedUiScripts() {}

    static String[] all() {
        List<String> out = new ArrayList<>(40);
        add(out, UiRuntimeBundle.domSafetyPrelude());
        addLayers(out, BaseSettingsUi.class, 1);
        addLayers(out, TimetableCoreUi.class, 10);
        add(out, WeekViewStabilityUi.script());
        addLayers(out, ScheduleDisplayUi.class, 8);
        addLayers(out, LocalizationUi.class, 5);
        add(out, ImportParserUi.script());
        addLayers(out, LocalizationFinalUi.class, 1);
        addLayers(out, WorkflowUi.class, 5);
        add(out, TemporalStateUi.script());
        add(out, StartupViewRecoveryUi.script());
        add(out, LunchIconCleanupUi.script());
        add(out, ImportReviewUi.script());
        return out.toArray(new String[0]);
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

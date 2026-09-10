package com.wokgui.schedulewidget;

/** Deterministic 6.44 repairs for legacy generated JavaScript that must not depend on whitespace. */
final class RuntimeRepair644 {
    private RuntimeRepair644() {}

    static String repairScheduleDisplayLayer0(String script) {
        if (script == null || script.isEmpty()) return script;

        final String recurring =
                "applySpecialCss();renderSpecialControls();ensureFullCoursePicker();wrapCourseSubmit();bindSyncToggle();repaintLiteralCourses();";
        final String stable =
                "applySpecialCss();renderSpecialControls();ensureFullCoursePicker();bindSyncToggle();repaintLiteralCourses();";
        if (!script.contains(recurring)) {
            throw new IllegalStateException("6.44 FineTune recurring submit wrapper anchor not found");
        }
        script = script.replace(recurring, stable);

        // FineTune still needs its colour-saving submit wrapper, but exactly once during
        // initial layer installation. Later refreshes must only update DOM/CSS state.
        final String refreshAnchor = "window.refreshFineTuneUi=refresh;";
        if (!script.contains(refreshAnchor)) {
            throw new IllegalStateException("6.44 FineTune refresh anchor not found");
        }
        script = script.replace(refreshAnchor, "wrapCourseSubmit();window.refreshFineTuneUi=refresh;");
        return script;
    }
}
package com.wokgui.schedulewidget;

/**
 * Repairs a few legacy callbacks that would otherwise escape the 6.51 action coordinator.
 *
 * <p>This deliberately targets only duplicate asynchronous work. Data mutations stay in the
 * feature that owns them; the coordinator merely batches the resulting UI refresh.
 */
final class LegacyChainRepair651 {
    private LegacyChainRepair651() {}

    static String repair(String script) {
        if (script == null || script.isEmpty()) return script;

        // Weekend add/remove already mutated the in-memory model and persisted it. Queue the
        // follow-up refresh inside the current action instead of scheduling a second pass.
        script = script.replace(
                "try{if(typeof render==='function')render()}catch(e){}\n                  setTimeout(refresh,0);",
                "try{if(typeof render==='function')render()}catch(e){}\n                  if(window.__edtActionChains651)window.__edtActionChains651.queueLocalRefresh(refresh);else setTimeout(refresh,0);");

        // Profile switching reloads the timetable. Advanced controls are refreshed once when
        // the action commits, not on a second timer after the reload.
        script = script.replace(
                "setTimeout(refreshAdvancedFeatures,18)",
                "(window.__edtActionChains651?window.__edtActionChains651.queueAdvancedRefresh():setTimeout(refreshAdvancedFeatures,18))");

        // School-calendar changes used to schedule two independent post-action passes.
        script = script.replace(
                "if(window.refreshAdvancedFeatures)setTimeout(window.refreshAdvancedFeatures,20);\n                  if(typeof render==='function')setTimeout(render,30);",
                "if(window.__edtActionChains651)window.__edtActionChains651.schoolCalendarChanged();\n                  else{if(window.refreshAdvancedFeatures)setTimeout(window.refreshAdvancedFeatures,20);if(typeof render==='function')setTimeout(render,30);}");

        // Break labels need live visual feedback, not three persistence triggers. Keep the
        // input path visual-only and persist once on change.
        script = script.replace(
                "input.addEventListener('input',queuePersist);\n                      input.addEventListener('change',applyBreakSettingsV9);\n                      input.addEventListener('blur',applyBreakSettingsV9);",
                "input.addEventListener('input',()=>{syncBreakStateFromControls();syncBreakCells()});\n                      input.addEventListener('change',applyBreakSettingsV9);");

        // Edit history can use its already-maintained last snapshot as the true pre-save
        // state. This removes one full exportState()/JSON serialization from every save and
        // makes undo represent the previous persisted state rather than the already-mutated one.
        script = script.replace("const before=snapshot();", "const before=lastSnapshot||snapshot();");
        script = script.replace(
                "wrapSave();lastSnapshot=snapshot();ensureButtons();refresh();",
                "wrapSave();lastSnapshot=snapshot();window.__edtSyncEditHistory651=function(){lastSnapshot=snapshot()};ensureButtons();refresh();");

        return script;
    }
}

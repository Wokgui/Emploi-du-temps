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

        // Break labels historically persisted on input, change and blur. Do these replacements
        // line by line rather than matching a whitespace-sensitive three-line block: chunks are
        // normalized before this repair and their indentation is not a stable API. Input remains
        // visual-only; the action coordinator owns the single persistence transaction on change.
        script = script.replace(
                "input.addEventListener('input',queuePersist);",
                "input.addEventListener('input',()=>{syncBreakStateFromControls();syncBreakCells()});");
        script = script.replace(
                "input.addEventListener('blur',applyBreakSettingsV9);",
                "/* 6.51: change is the sole break-label persistence trigger. */");

        // Stability73 owns the physical pointer-up/click route of the visible week-cycle bar and
        // stops propagation before the generic click router. Hand modes 1/2/3 to the 6.51
        // transaction coordinator at that exact ownership point instead of adding another event
        // listener. Mode 4 remains on the legacy path because 6.51's current contract is 1–3.
        script = script.replace(
                "const n=Number(b.dataset.m);lastPointerAt=Date.now();lastPointerMode=n;applyMode(n);",
                "const n=Number(b.dataset.m);lastPointerAt=Date.now();lastPointerMode=n;const chain=window.__edtActionChains651;if(n!==4&&chain&&typeof chain.runClick==='function')chain.runClick(b,e,function(){});else applyMode(n);");
        script = script.replace(
                "if(Date.now()-lastPointerAt<700&&n===lastPointerMode)return;\n      applyMode(n);",
                "if(Date.now()-lastPointerAt<700&&n===lastPointerMode)return;\n      const chain=window.__edtActionChains651;if(n!==4&&chain&&typeof chain.runClick==='function')chain.runClick(b,e,function(){});else applyMode(n);");

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

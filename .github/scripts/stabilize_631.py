from pathlib import Path
import re

ROOT = Path('android-widget/app/src/main/java/com/wokgui/schedulewidget')
main_path = ROOT / 'MainActivity.java'
workflow_path = ROOT / 'WorkflowUi.java'
gradle_path = Path('android-widget/app/build.gradle')

main = main_path.read_text(encoding='utf-8')

# Prevent repeated parsing/execution of the complete ~460 KB WebView runtime on every resume.
main = main.replace(
    '    private boolean pageLoaded = false;\n',
    '    private boolean pageLoaded = false;\n    private boolean uiInjected = false;\n    private boolean uiInjectionInFlight = false;\n'
)

main = main.replace(
    '                pageLoaded = true;\n                primeWeekBadge();\n                if (!forceWeekOpening) applyOpenMode();\n                injectPersonalizationUi();',
    '                pageLoaded = true;\n                uiInjected = false;\n                uiInjectionInFlight = false;\n                primeWeekBadge();\n                if (!forceWeekOpening) applyOpenMode();\n                ensureUiReady();'
)

main = main.replace('value -> injectPersonalizationUi());', 'value -> ensureUiReady());')
main = main.replace('            injectPersonalizationUi();', '            ensureUiReady();')

old_resume = '''    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.evaluateJavascript(
                    "if(window.reloadSchedule){reloadSchedule();}",
                    value -> {
                        primeWeekBadge();
                        if (!forceWeekOpening) applyOpenMode();
                        injectPersonalizationUi();
                    }
            );
        }
    }
'''
new_resume = '''    @Override
    protected void onResume() {
        super.onResume();
        if (webView == null || !pageLoaded) return;
        webView.evaluateJavascript(
                "if(window.reloadSchedule){reloadSchedule();}",
                value -> {
                    primeWeekBadge();
                    if (!forceWeekOpening) applyOpenMode();
                    ensureUiReady();
                }
        );
    }
'''
if old_resume in main:
    main = main.replace(old_resume, new_resume)

main = main.replace(
    '''        pageLoaded = false;
        hideWebViewUntilWeekIsReady();
        webView.post(webView::reload);''',
    '''        pageLoaded = false;
        uiInjected = false;
        uiInjectionInFlight = false;
        hideWebViewUntilWeekIsReady();
        webView.post(webView::reload);'''
)

old_inject = '''    private void injectPersonalizationUi() {
    if (webView == null) return;
    webView.evaluateJavascript(
            "(function(){if(window.__settingsV3&&!document.getElementById('settingsBtn')){var m=document.getElementById('settingsModal');if(m)m.remove();window.__settingsV3=false;}})();",
            ignored -> webView.evaluateJavascript(UiRuntimeBundle.script(), value -> {
                    primeWeekBadge();
                    if (forceWeekOpening) settleWeekAndReveal();
                    else revealWebViewStable();
            })
    );
}
'''
new_inject = '''    private void ensureUiReady() {
        if (webView == null || !pageLoaded) return;
        if (uiInjected) {
            finishUiReady();
            return;
        }
        if (uiInjectionInFlight) return;
        uiInjectionInFlight = true;
        webView.evaluateJavascript(
                "(function(){if(window.__settingsV3&&!document.getElementById('settingsBtn')){var m=document.getElementById('settingsModal');if(m)m.remove();window.__settingsV3=false;}})();",
                ignored -> webView.evaluateJavascript(UiRuntimeBundle.script(), value -> {
                    uiInjectionInFlight = false;
                    uiInjected = true;
                    finishUiReady();
                })
        );
    }

    private void finishUiReady() {
        if (webView == null || !pageLoaded) return;
        primeWeekBadge();
        if (forceWeekOpening) settleWeekAndReveal();
        else revealWebViewStable();
    }
'''
if old_inject not in main:
    raise SystemExit('MainActivity injection block not found')
main = main.replace(old_inject, new_inject)
main_path.write_text(main, encoding='utf-8')

# WorkflowUi still carried the superseded 6.25 one-level undo implementation in layer0,
# while layer2 already provides the proper Undo/Redo history. Remove the old save wrapper so
# every timetable save is intercepted only once.
workflow = workflow_path.read_text(encoding='utf-8')
workflow = workflow.replace(
    '                let arranging=false,arrangeTimer=0,undoSuppress=false,lastSaveAt=0;\n                const undoStack=[];\n                let lastSnapshot=\'\',lastFingerprint=\'\';',
    '                let arranging=false,arrangeTimer=0;'
)
workflow = re.sub(
    r'''\n                function currentFullSnapshot85\(\)\{.*?\n                function ensureDuplicate85\(\)\{''',
    '\n                function ensureDuplicate85(){',
    workflow,
    count=1,
    flags=re.S
)
workflow = workflow.replace(
    "                function refresh(){ensureUndo85();ensureDuplicate85();arrangeAdvanced85();refreshUndoButton85();setVersion85()}\n",
    "                function refresh(){ensureDuplicate85();arrangeAdvanced85();setVersion85()}\n"
)
workflow = workflow.replace(
    "                wrapSave85();wrapOpenEditor85();ensureUndo85();ensureDuplicate85();installFastPress85();\n",
    "                wrapOpenEditor85();ensureDuplicate85();installFastPress85();\n"
)
workflow = workflow.replace('                initUndo85();refresh();\n', '                refresh();\n')
workflow_path.write_text(workflow, encoding='utf-8')

# Align all visible runtime version labels with 6.31.
for path in ROOT.glob('*.java'):
    src = path.read_text(encoding='utf-8')
    src = re.sub(r"const APP_VERSION='6\.[0-9]+';", "const APP_VERSION='6.31';", src)
    src = re.sub(r"Version 6\.[0-9]+", "Version 6.31", src)
    path.write_text(src, encoding='utf-8')

gradle = gradle_path.read_text(encoding='utf-8')
gradle = re.sub(r"versionName '6\.[0-9]+'", "versionName '6.31'", gradle)
gradle_path.write_text(gradle, encoding='utf-8')

# Static regression contracts for the stabilization pass.
main = main_path.read_text(encoding='utf-8')
workflow = workflow_path.read_text(encoding='utf-8')
checks = {
    'single runtime injection guard': 'private boolean uiInjected = false;' in main and 'if (uiInjected)' in main,
    'resume waits for page': 'if (webView == null || !pageLoaded) return;' in main,
    'language reload resets injection': 'uiInjectionInFlight = false;' in main,
    'legacy undo wrapper removed': 'function wrapSave85()' not in workflow and 'undoStack' not in workflow,
    'proper undo/redo retained': 'const undo=[],redo=[];' in workflow and 'function doUndo()' in workflow and 'function doRedo()' in workflow,
}
failed = [name for name, ok in checks.items() if not ok]
if failed:
    raise SystemExit('Stabilization contracts failed: ' + ', '.join(failed))
print('6.31 stabilization contracts passed:', ', '.join(checks))

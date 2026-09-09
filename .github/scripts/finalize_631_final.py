from pathlib import Path
import re

ROOT = Path('.')
MAIN = ROOT / 'android-widget/app/src/main/java/com/wokgui/schedulewidget/MainActivity.java'
GRADLE = ROOT / 'android-widget/app/build.gradle'

main = MAIN.read_text(encoding='utf-8')

old_new_intent = '''        hideWebViewUntilWeekIsReady();
        primeWeekBadge();
        if (forceWeekOpening && webView != null) {
            webView.evaluateJavascript("if(window.reloadSchedule){reloadSchedule();}", value -> ensureUiReady());
        } else {
            applyOpenMode();
            ensureUiReady();
        }
'''
new_new_intent = '''        if (webView == null || !pageLoaded) return;
        hideWebViewUntilWeekIsReady();
        refreshScheduleAndUi();
'''
if old_new_intent in main:
    main = main.replace(old_new_intent, new_new_intent, 1)
elif new_new_intent not in main:
    raise SystemExit('Expected onNewIntent block not found')

old_resume = '''        if (webView != null) {
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
new_resume = '''        if (webView == null || !pageLoaded) return;
        refreshScheduleAndUi();
'''
if old_resume in main:
    main = main.replace(old_resume, new_resume, 1)
elif new_resume not in main:
    raise SystemExit('Expected onResume block not found')

helper = '''    private void refreshScheduleAndUi() {
        if (webView == null || !pageLoaded) return;
        webView.evaluateJavascript(
                "if(window.reloadSchedule){reloadSchedule();}",
                value -> {
                    if (webView == null || !pageLoaded) return;
                    primeWeekBadge();
                    if (!forceWeekOpening) applyOpenMode();
                    ensureUiReady();
                }
        );
    }

'''
if 'private void refreshScheduleAndUi()' not in main:
    anchor = '    private void ensureUiReady() {\n'
    if anchor not in main:
        raise SystemExit('ensureUiReady anchor not found')
    main = main.replace(anchor, helper + anchor, 1)

main = main.replace(
    '    private void primeWeekBadge() {\n        if (webView == null || !AdvancedSettingsStore.json(this).optBoolean("singleWeek", false)) return;',
    '    private void primeWeekBadge() {\n        if (webView == null || !pageLoaded || !AdvancedSettingsStore.json(this).optBoolean("singleWeek", false)) return;',
    1,
)
main = main.replace(
    '    private void applyOpenMode() {\n        if (webView == null || getIntent() == null) return;',
    '    private void applyOpenMode() {\n        if (webView == null || !pageLoaded || getIntent() == null) return;',
    1,
)
main = main.replace(
    '    private void settleWeekAndReveal() {\n        if (webView == null) return;',
    '    private void settleWeekAndReveal() {\n        if (webView == null || !pageLoaded) return;',
    1,
)

if 'private void refreshScheduleAndUi()' not in main:
    raise SystemExit('refreshScheduleAndUi helper missing')
MAIN.write_text(main, encoding='utf-8')

gradle = GRADLE.read_text(encoding='utf-8')
gradle, n = re.subn(r"versionCode\s+(?:project\.hasProperty\('versionCode'\)\s*\?\s*project\.property\('versionCode'\)\.toInteger\(\)\s*:\s*\d+|\d+)", 'versionCode 631001', gradle, count=1)
if n != 1:
    raise SystemExit('versionCode line not found')
gradle = re.sub(r"versionName '6\.[0-9]+'", "versionName '6.31'", gradle, count=1)
GRADLE.write_text(gradle, encoding='utf-8')

print('6.31 app-source stabilization prepared')

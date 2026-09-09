from pathlib import Path
import re

VERSION = '6.30'
ROOT = Path('android-widget/app/src/main/java/com/wokgui/schedulewidget')


def extract_script(name: str) -> str:
    path = ROOT / f'{name}.java'
    if not path.exists():
        raise SystemExit(f'Missing source layer: {name}')
    source = path.read_text(encoding='utf-8')
    match = re.search(r'return\s+"""(.*?)"""\s*;', source, re.S)
    if not match:
        raise SystemExit(f'Unable to extract script from {name}')
    body = match.group(1)
    body = re.sub(r"const APP_VERSION='6\.[0-9]+';", f"const APP_VERSION='{VERSION}';", body)
    body = re.sub(r"Version 6\.[0-9]+", f"Version {VERSION}", body)
    return body


def write_module(class_name: str, sources: list[str], description: str) -> None:
    bodies = [(name, extract_script(name)) for name in sources]
    capacity_kb = max(64, sum(len(body) for _, body in bodies) // 1024 + 32)
    lines = [
        'package com.wokgui.schedulewidget;',
        '',
        f'/** {description} */',
        f'final class {class_name} {{',
        f'    private {class_name}() {{}}',
        '',
        '    static String script() {',
        f'        StringBuilder out = new StringBuilder({capacity_kb} * 1024);',
    ]
    for i, (name, _) in enumerate(bodies):
        lines.append(f"        out.append(layer{i}()).append('\\n'); // {name}")
    lines += ['        return out.toString();', '    }', '']
    for i, (name, body) in enumerate(bodies):
        lines += [
            f'    // Former {name}; kept as an isolated constant to avoid JVM string-size limits.',
            f'    private static String layer{i}() {{',
            '        return """' + body + '""";',
            '    }',
            '',
        ]
    lines.append('}')
    lines.append('')
    (ROOT / f'{class_name}.java').write_text('\n'.join(lines), encoding='utf-8')


# Preserve the exact verified runtime order. OcrImport80Ui intentionally stays between
# Stability80Ui and Stability81Ui, as it was before the earlier wrapper cleanup.
GROUPS = {
    'BaseSettingsUi': (
        ['PersonalizationUi2'],
        'Base settings UI and its persistent controls.'
    ),
    'TimetableUi': (
        [
            'WeekendUi', 'FinalPolishUi', 'FinalPolishLateUi', 'AdvancedFeaturesUi',
            'UiPolishAndSchoolCalendarUi', 'CourseColorUi', 'PaletteSelectorUi',
            'LunchBreakUi', 'DoubleLunchUi', 'BulkCourseUi', 'WeekViewStabilityUi',
            'FineTuneUi', 'CycleLunchFixUi', 'Stability69Ui', 'Stability70Ui',
            'Stability71Ui', 'Stability72Ui', 'Stability73Ui', 'Stability74Ui'
        ],
        'Timetable rendering, week cycles, breaks, colors and interaction behavior.'
    ),
    'LocalizationUi': (
        ['Localization75Ui', 'LayoutLanguage77Ui', 'Stability78Ui', 'Stability79Ui', 'Stability80Ui'],
        'Application localization and downloaded-language support.'
    ),
    'ImportParserUi': (
        ['OcrImport80Ui'],
        'Timetable-photo OCR parsing.'
    ),
    'LocalizationFinalUi': (
        ['Stability81Ui'],
        'Final dynamic localization and language-download bindings.'
    ),
    'WorkflowUi': (
        ['Workflow85Ui', 'SettingsLayoutUi', 'EditHistoryUi', 'OcrPreviewUi', 'SettingsResetUi'],
        'Editing workflow, safe import preview, compact settings and undo/redo.'
    ),
}

# Extract everything before overwriting any existing semantic wrapper.
for class_name, (sources, description) in GROUPS.items():
    write_module(class_name, sources, description)

bundle = '''package com.wokgui.schedulewidget;

/** Single WebView injection entry point. Runtime behavior is grouped by responsibility. */
final class UiRuntimeBundle {
    private UiRuntimeBundle() {}

    static String script() {
        StringBuilder out = new StringBuilder(460 * 1024);
        out.append(BaseSettingsUi.script()).append('\\n');
        out.append(TimetableUi.script()).append('\\n');
        out.append(LocalizationUi.script()).append('\\n');
        out.append(ImportParserUi.script()).append('\\n');
        out.append(LocalizationFinalUi.script()).append('\\n');
        out.append(WorkflowUi.script()).append('\\n');
        return out.toString();
    }
}
'''
(ROOT / 'UiRuntimeBundle.java').write_text(bundle, encoding='utf-8')

old_sources = set()
for sources, _ in GROUPS.values():
    old_sources.update(sources)
outputs = set(GROUPS) | {'UiRuntimeBundle'}
for name in sorted(old_sources - outputs):
    path = ROOT / f'{name}.java'
    if path.exists():
        path.unlink()

# Remove temporary wrapper classes superseded by the semantic root modules.
for name in ['TimetableRuntimeUi', 'FeatureRuntimeUi', 'LocalizationRuntimeUi']:
    if name not in outputs:
        path = ROOT / f'{name}.java'
        if path.exists():
            path.unlink()

# Align any version label remaining in native/extension Java files.
for path in ROOT.glob('*.java'):
    source = path.read_text(encoding='utf-8')
    source = re.sub(r"const APP_VERSION='6\.[0-9]+';", f"const APP_VERSION='{VERSION}';", source)
    source = re.sub(r"Version 6\.[0-9]+", f"Version {VERSION}", source)
    path.write_text(source, encoding='utf-8')

gradle = Path('android-widget/app/build.gradle')
source = gradle.read_text(encoding='utf-8')
source = re.sub(r"versionName '6\.[0-9]+'", f"versionName '{VERSION}'", source)
gradle.write_text(source, encoding='utf-8')

print('Generated semantic modules:', ', '.join(GROUPS))
print('Removed legacy/versioned UI classes:', len(old_sources - outputs))

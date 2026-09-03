package com.wokgui.schedulewidget;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PronoteHomework {
    private static final Pattern SIMPLE = Pattern.compile("[3-6]G[0-9](?:BIL)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern GROUPED = Pattern.compile("([3-6]G)([0-9](?:-[0-9])+)", Pattern.CASE_INSENSITIVE);

    private PronoteHomework() {}

    static HomeworkInfo forCourse(Context context, String label) {
        HomeworkInfo info = new HomeworkInfo(label == null ? "" : label);
        Set<String> tokens = tokens(label);
        if (tokens.isEmpty()) return info;

        try {
            JSONObject root = new JSONObject(PronoteStore.getSnapshot(context));
            JSONArray blocks = root.optJSONArray("blocks");
            if (blocks == null) return info;

            LinkedHashSet<String> lesson = new LinkedHashSet<>();
            LinkedHashSet<String> homework = new LinkedHashSet<>();
            LinkedHashSet<String> fallback = new LinkedHashSet<>();

            for (int i = 0; i < blocks.length(); i++) {
                JSONObject b = blocks.optJSONObject(i);
                if (b == null) continue;
                String text = clean(b.optString("text", ""));
                String contextText = clean(b.optString("context", ""));
                String combined = clean(contextText + " " + text);
                String normalized = normalize(combined);
                boolean matches = false;
                for (String token : tokens) {
                    if (normalized.contains(normalize(token))) {
                        matches = true;
                        break;
                    }
                }
                if (!matches) continue;

                String lower = stripAccents(combined).toLowerCase(Locale.ROOT);
                String display = shorten(preferText(text, combined), 280);
                if (display.isEmpty()) continue;

                boolean isHomework = lower.contains("travail a faire")
                        || lower.contains("travaux a faire")
                        || lower.contains("devoir")
                        || lower.contains("a faire pour")
                        || lower.contains("prochaine fois")
                        || lower.contains("pour le prochain cours");
                boolean isLesson = lower.contains("cahier de textes")
                        || lower.contains("contenu du cours")
                        || lower.contains("contenu de la seance")
                        || lower.contains("seance")
                        || lower.contains("cours");

                if (isHomework) homework.add(display);
                if (isLesson && !isHomework) lesson.add(display);
                if (!isHomework && !isLesson) fallback.add(display);
            }

            if (lesson.isEmpty()) lesson.addAll(fallback);
            info.lesson = join(lesson, 430);
            info.nextHomework = join(homework, 430);
            info.hasData = !info.lesson.isEmpty() || !info.nextHomework.isEmpty();
        } catch (Exception ignored) {
        }
        return info;
    }

    static String toJson(Context context, String label) {
        HomeworkInfo info = forCourse(context, label);
        JSONObject out = new JSONObject();
        try {
            out.put("label", info.label);
            out.put("lesson", info.lesson);
            out.put("nextHomework", info.nextHomework);
            out.put("hasData", info.hasData);
            out.put("importedAt", PronoteStore.getImportedAt(context));
        } catch (Exception ignored) {
        }
        return out.toString();
    }

    private static Set<String> tokens(String label) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        if (label == null) return out;
        Matcher m = SIMPLE.matcher(label.toUpperCase(Locale.ROOT));
        while (m.find()) out.add(m.group());
        Matcher g = GROUPED.matcher(label.toUpperCase(Locale.ROOT));
        if (g.find()) {
            String prefix = g.group(1);
            String[] nums = g.group(2).split("-");
            for (String n : nums) out.add(prefix + n);
        }
        return out;
    }

    private static String preferText(String text, String combined) {
        String t = clean(text);
        return t.length() >= 10 ? t : clean(combined);
    }

    private static String clean(String value) {
        if (value == null) return "";
        return value.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ')
                .replaceAll(" +", " ").trim();
    }

    private static String stripAccents(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }

    private static String normalize(String value) {
        return stripAccents(clean(value)).toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

    private static String shorten(String value, int max) {
        String v = clean(value);
        return v.length() <= max ? v : v.substring(0, Math.max(0, max - 1)) + "…";
    }

    private static String join(Set<String> values, int max) {
        if (values.isEmpty()) return "";
        List<String> items = new ArrayList<>(values);
        StringBuilder out = new StringBuilder();
        for (String s : items) {
            if (out.length() > 0) out.append(" · ");
            out.append(s);
            if (out.length() >= max) break;
        }
        return shorten(out.toString(), max);
    }

    static final class HomeworkInfo {
        final String label;
        String lesson = "";
        String nextHomework = "";
        boolean hasData = false;

        HomeworkInfo(String label) {
            this.label = label;
        }
    }
}

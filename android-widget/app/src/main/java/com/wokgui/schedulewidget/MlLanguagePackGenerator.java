package com.wokgui.schedulewidget;

import android.content.Context;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

final class MlLanguagePackGenerator {
    interface Callback {
        void onSuccess(String packJson);
        void onFailure(String message);
    }

    private static final class Entry {
        final String section;
        final String key;
        final String source;
        Entry(String section, String key, String source) {
            this.section = section;
            this.key = key;
            this.source = source;
        }
    }

    private MlLanguagePackGenerator() {}

    static String supportedLanguagesJson() {
        JSONArray out = new JSONArray();
        try {
            List<String> codes = new ArrayList<>(TranslateLanguage.getAllLanguages());
            codes.sort(String::compareTo);
            for (String code : codes) {
                if (code == null || code.isEmpty() || "fr".equals(code)) continue;
                Locale locale = Locale.forLanguageTag(code);
                String name = locale.getDisplayLanguage(Locale.FRENCH);
                if (name == null || name.trim().isEmpty()) name = code;
                JSONObject item = new JSONObject();
                item.put("code", code);
                item.put("name", name.substring(0, 1).toUpperCase(Locale.FRENCH) + name.substring(1));
                out.put(item);
            }
        } catch (Exception ignored) {}
        return out.toString();
    }

    static void generate(Context context, String requestedCode, String requestedName, Callback callback) {
        final String code = requestedCode == null ? "" : requestedCode.trim().toLowerCase(Locale.ROOT);
        final String target = TranslateLanguage.fromLanguageTag(code);
        if (target == null || "fr".equals(code)) {
            callback.onFailure("Langue non prise en charge");
            return;
        }

        final JSONObject template;
        try {
            template = new JSONObject(readAsset(context, "language_template_fr.json"));
        } catch (Exception e) {
            callback.onFailure("Modèle de traduction introuvable");
            return;
        }

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.FRENCH)
                .setTargetLanguage(target)
                .build();
        final Translator translator = Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused -> translateTemplate(context, translator, code, requestedName, template, callback))
                .addOnFailureListener(error -> {
                    translator.close();
                    callback.onFailure("Téléchargement du modèle impossible");
                });
    }

    private static void translateTemplate(Context context, Translator translator, String code, String requestedName,
                                          JSONObject template, Callback callback) {
        try {
            JSONObject sourceStrings = template.optJSONObject("strings");
            JSONObject sourceWidget = template.optJSONObject("widget");
            JSONObject targetStrings = new JSONObject();
            JSONObject targetWidget = new JSONObject();
            List<Entry> entries = new ArrayList<>();
            addEntries(entries, "strings", sourceStrings);
            addEntries(entries, "widget", sourceWidget);

            if (entries.isEmpty()) {
                translator.close();
                callback.onFailure("Aucun texte à traduire");
                return;
            }

            Locale locale = Locale.forLanguageTag(code);
            String nativeName = locale.getDisplayLanguage(locale);
            if (nativeName == null || nativeName.trim().isEmpty()) nativeName = requestedName;
            if (nativeName == null || nativeName.trim().isEmpty()) nativeName = code;
            final String packName = nativeName.substring(0, 1).toUpperCase(locale) + nativeName.substring(1);

            AtomicInteger remaining = new AtomicInteger(entries.size());
            AtomicBoolean finished = new AtomicBoolean(false);
            for (Entry entry : entries) {
                translator.translate(entry.source)
                        .addOnSuccessListener(translated -> {
                            try {
                                JSONObject section = "widget".equals(entry.section) ? targetWidget : targetStrings;
                                section.put(entry.key, translated == null || translated.trim().isEmpty() ? entry.source : translated.trim());
                            } catch (Exception ignored) {}
                            completeOne(context, translator, code, packName, targetStrings, targetWidget, remaining, finished, callback);
                        })
                        .addOnFailureListener(error -> {
                            try {
                                JSONObject section = "widget".equals(entry.section) ? targetWidget : targetStrings;
                                section.put(entry.key, entry.source);
                            } catch (Exception ignored) {}
                            completeOne(context, translator, code, packName, targetStrings, targetWidget, remaining, finished, callback);
                        });
            }
        } catch (Exception e) {
            translator.close();
            callback.onFailure("Création de la langue impossible");
        }
    }

    private static void completeOne(Context context, Translator translator, String code, String name,
                                    JSONObject strings, JSONObject widget, AtomicInteger remaining,
                                    AtomicBoolean finished, Callback callback) {
        if (remaining.decrementAndGet() != 0 || !finished.compareAndSet(false, true)) return;
        try {
            JSONObject pack = new JSONObject();
            pack.put("version", 1);
            pack.put("code", code);
            pack.put("name", name);
            pack.put("strings", strings);
            pack.put("widget", widget);
            String raw = pack.toString();
            if (!LanguagePackStore.save(context, raw)) throw new IllegalStateException("save");
            callback.onSuccess(raw);
        } catch (Exception e) {
            callback.onFailure("Enregistrement de la langue impossible");
        } finally {
            translator.close();
        }
    }

    private static void addEntries(List<Entry> out, String section, JSONObject object) {
        if (object == null) return;
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String source = object.optString(key, "").trim();
            if (!source.isEmpty()) out.add(new Entry(section, key, source));
        }
    }

    private static String readAsset(Context context, String name) throws Exception {
        StringBuilder out = new StringBuilder();
        try (InputStream in = context.getAssets().open(name);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) out.append(line).append('\n');
        }
        return out.toString();
    }
}

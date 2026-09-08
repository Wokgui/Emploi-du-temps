package com.wokgui.schedulewidget;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

final class NativeLanguageDownloader {
    private static final String TRUSTED_HOST = "raw.githubusercontent.com";
    private static final String TRUSTED_PREFIX = "/Wokgui/Emploi-du-temps/main/language-packs/";
    private static final String CATALOG = "https://raw.githubusercontent.com/Wokgui/Emploi-du-temps/main/language-packs/catalog.json";

    private NativeLanguageDownloader() {}

    static String catalog() {
        return fetch(CATALOG);
    }

    static String downloadAndSave(Context context, String rawUrl) {
        String raw = fetch(rawUrl);
        if (raw.isEmpty()) return "";
        try {
            JSONObject root = new JSONObject(raw);
            String code = root.optString("code", "").trim();
            String name = root.optString("name", "").trim();
            JSONObject strings = root.optJSONObject("strings");
            if (code.isEmpty() || name.isEmpty() || strings == null || strings.length() == 0) return "";
            return LanguagePackStore.save(context, raw) ? raw : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String fetch(String rawUrl) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(rawUrl == null ? "" : rawUrl);
            if (!"https".equalsIgnoreCase(url.getProtocol())) return "";
            if (!TRUSTED_HOST.equalsIgnoreCase(url.getHost())) return "";
            if (url.getPath() == null || !url.getPath().startsWith(TRUSTED_PREFIX)) return "";

            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(6000);
            connection.setReadTimeout(6000);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "EmploiDuTemps-Android");
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) return "";

            StringBuilder out = new StringBuilder();
            try (InputStream in = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) out.append(line).append('\n');
            }
            return out.toString();
        } catch (Exception ignored) {
            return "";
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}

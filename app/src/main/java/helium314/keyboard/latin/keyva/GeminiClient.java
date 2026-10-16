// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.keyva;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;

public final class GeminiClient {
    interface ConnectionFactory { HttpsURLConnection open(URL url) throws Exception; }
    private final ConnectionFactory factory;
    public GeminiClient() { this(url -> (HttpsURLConnection) url.openConnection()); }
    GeminiClient(ConnectionFactory factory) { this.factory = factory; }
    private volatile HttpsURLConnection connection;
    private final AtomicBoolean cancelled = new AtomicBoolean();
    public void cancel() { cancelled.set(true); HttpsURLConnection c = connection; if (c != null) c.disconnect(); }
    public String correct(String text, String mode, KeyStoreConfig config) throws Exception {
        if (text.trim().isEmpty() || text.length() > 12000) throw new IllegalArgumentException("Seleccioná entre 1 y 12000 caracteres");
        if (!config.enabled()) throw new IllegalStateException("Activá la IA en ajustes");
        String key = config.read(); if (key.isEmpty()) throw new IllegalStateException("Configurá una clave nueva en ajustes");
        String model = config.model(); if (!model.matches("gemini-[a-zA-Z0-9._-]{1,80}")) throw new IllegalArgumentException("Modelo inválido");
        JSONObject body = new JSONObject().put("systemInstruction", new JSONObject().put("parts", new JSONArray().put(new JSONObject().put("text", AiPolicy.instruction(mode)))))
            .put("contents", new JSONArray().put(new JSONObject().put("role", "user").put("parts", new JSONArray().put(new JSONObject().put("text", text)))))
            .put("generationConfig", new JSONObject().put("temperature", 0.1).put("maxOutputTokens", 8192));
        HttpsURLConnection c = factory.open(new URL("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent"));
        connection = c;
        try {
            if (cancelled.get()) throw new InterruptedException();
            c.setConnectTimeout(15000); c.setReadTimeout(45000); c.setInstanceFollowRedirects(false);
            c.setRequestMethod("POST"); c.setDoOutput(true); c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            c.setRequestProperty("x-goog-api-key", key);
            try (java.io.OutputStream out = c.getOutputStream()) { out.write(body.toString().getBytes(StandardCharsets.UTF_8)); }
            int status = c.getResponseCode();
            if (status != 200) throw new IllegalStateException(status == 429 ? "Cuota agotada. Intentá más tarde; no se activará facturación."
                : status == 401 || status == 403 ? "Clave inválida o sin acceso (" + status + ")" : "Gemini no disponible (" + status + ")");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (InputStream in = c.getInputStream()) {
                byte[] buffer = new byte[4096]; int n;
                while ((n = in.read(buffer)) != -1) { if (cancelled.get()) throw new InterruptedException(); bytes.write(buffer, 0, n); if (bytes.size() > 262144) throw new IllegalStateException("Respuesta demasiado grande"); }
            }
            JSONObject candidate = new JSONObject(bytes.toString("UTF-8")).getJSONArray("candidates").getJSONObject(0);
            if (!"STOP".equals(candidate.optString("finishReason"))) throw new IllegalStateException("Respuesta incompleta o bloqueada. Se conserva tu texto.");
            JSONArray parts = candidate.getJSONObject("content").getJSONArray("parts"); StringBuilder result = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) if (!parts.getJSONObject(i).optBoolean("thought", false)) result.append(parts.getJSONObject(i).optString("text", ""));
            String output = result.toString().trim();
            if (output.isEmpty() || output.length() > 16000) throw new IllegalStateException("Respuesta no válida");
            if (cancelled.get()) throw new InterruptedException(); return output;
        } finally { c.disconnect(); connection = null; }
    }
}

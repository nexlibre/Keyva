// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.keyva;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AiSettingsActivity extends Activity {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private GeminiClient client;
    private TextView status;
    private EditText key, model;
    private Switch enabled;
    private KeyStoreConfig config;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        config = new KeyStoreConfig(this);
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(24,24,24,24);
        ScrollView scroll = new ScrollView(this); scroll.addView(box); setContentView(scroll);
        TextView title = new TextView(this); title.setText("Keyva · Voz e inteligencia artificial"); title.setTextSize(22); box.addView(title);
        TextView privacy = new TextView(this); privacy.setText("La escritura normal funciona sin red. Solo el texto que seleccionés y confirmés se enviará a Google al pulsar Corregir. El nivel gratuito puede usar contenido para mejorar sus productos; evitá datos privados. Las cuotas y modelos disponibles dependen de tu cuenta. Usá un proyecto sin facturación. Keyva no puede impedir cobros si configurás una clave de un proyecto con facturación.\n\nRevocá la clave compartida anteriormente. Introducí una nueva aquí; no la enviés por chat. Se cifra con Android Keystore y no se incluye en copias de seguridad.\n"); box.addView(privacy);
        enabled = new Switch(this); enabled.setText("Habilitar Gemini (opcional)"); enabled.setChecked(config.enabled()); box.addView(enabled);
        key = new EditText(this); key.setHint("Clave nueva (vacío conserva la guardada)"); key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); key.setImportantForAutofill(android.view.View.IMPORTANT_FOR_AUTOFILL_NO); box.addView(key);
        model = new EditText(this); model.setSingleLine(true); model.setText(config.model()); model.setHint("Modelo Gemini disponible en tu cuota gratuita"); box.addView(model);
        status = new TextView(this); box.addView(status);
        add(box,"Guardar", () -> save());
        add(box,"Probar conexión (envía solamente «hola»)", () -> {
            if (!save()) return;
            if (client != null) client.cancel(); client = new GeminiClient(); GeminiClient request = client;
            status.setText("Comprobando…"); worker.execute(() -> {
                try { request.correct("hola", "Corrección básica", config); runOnUiThread(() -> { if (!isDestroyed()) status.setText("Conexión correcta"); }); }
                catch (Exception e) { runOnUiThread(() -> { if (!isDestroyed()) status.setText("No se pudo conectar. Revisá clave, modelo, red y cuota gratuita."); }); }
            });
        });
        add(box,"Cancelar prueba", () -> { if (client != null) client.cancel(); status.setText("Cancelado"); });
        add(box,"Eliminar clave y desactivar IA", () -> { try { if (client != null) client.cancel(); config.clear(); key.setText(""); enabled.setChecked(false); status.setText("Clave eliminada"); } catch (Exception e) { status.setText("No se pudo eliminar"); } });
        add(box,"Permitir micrófono", () -> requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 7));
        TextView voice = new TextView(this); voice.setText("Después del permiso, volvé al campo de escritura y pulsá Voz. El reconocimiento local depende del dispositivo y del idioma instalado. Si falla, no se cambia automáticamente a reconocimiento en línea. No se guarda audio.\n\nLa corrección usa la selección actual; seleccioná el mensaje antes de pulsar IA. Deshacer solo opera inmediatamente sobre el último reemplazo, sin cambios de selección ni escritura."); box.addView(voice);
        add(box,"Volver", this::finish);
    }
    private boolean save() {
        try { config.save(key.getText().toString().trim(), model.getText().toString().trim(), enabled.isChecked()); key.setText(""); status.setText("Configuración guardada"); return true; }
        catch (Exception e) { status.setText("No se pudo guardar. Revisá el modelo o desbloqueá el teléfono."); return false; }
    }
    private void add(LinearLayout box, String label, Runnable action) { Button b = new Button(this); b.setText(label); box.addView(b); b.setOnClickListener(v -> action.run()); }
    @Override public void onDestroy() { if (client != null) client.cancel(); worker.shutdownNow(); key.setText(""); super.onDestroy(); }
}

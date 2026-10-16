// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.keyva;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.*;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import helium314.keyboard.latin.LatinIME;
import helium314.keyboard.latin.R;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Per-editor ephemeral state. Nothing is persisted except opt-in configuration. */
public final class AiController {
    private final LatinIME ime;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private int generation;
    private SpeechRecognizer recognizer;
    private GeminiClient client;
    private AlertDialog dialog;
    private String undoBefore, undoAfter;
    private int undoCursor = -1;
    private InputConnection undoConnection;
    private View root;
    public AiController(LatinIME ime) { this.ime = ime; }
    public void attach(View view) {
        reset(); root = view;
        LinearLayout frame = view.findViewById(R.id.main_keyboard_frame);
        LinearLayout bar = new LinearLayout(ime); bar.setOrientation(LinearLayout.HORIZONTAL); bar.setBackgroundColor(Color.BLACK);
        add(bar,"Voz", this::dictate); add(bar,"IA", this::correctSelection); add(bar,"Deshacer", this::undo); add(bar,"Ajustes IA", this::settings);
        // InputView is retained, so HeliBoard's insets, suggestions, clipboard and emoji views remain intact.
        frame.addView(bar, 0, new LinearLayout.LayoutParams(-1, (int)(48 * ime.getResources().getDisplayMetrics().density)));
    }
    private void add(LinearLayout bar, String text, Runnable action) {
        Button b = new Button(ime); b.setText(text); b.setTextSize(12); b.setTextColor(Color.WHITE); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(25,25,25)));
        b.setPadding(0,0,0,0); b.setMinWidth(0); b.setContentDescription(text);
        bar.addView(b, new LinearLayout.LayoutParams(0,-1,1)); b.setOnClickListener(v -> action.run());
    }
    private void settings() { reset(); ime.startActivity(new Intent(ime,AiSettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
    private void toast(String message) { Toast.makeText(ime,message,Toast.LENGTH_LONG).show(); }
    private boolean safe() {
        if (!AiPolicy.allowed(ime.getCurrentInputEditorInfo())) { toast("Voz e IA no disponibles en campos sensibles o privados"); return false; }
        return ime.getCurrentInputConnection() != null;
    }
    private boolean valid(int token, InputConnection connection) { return token == generation && AiPolicy.allowed(ime.getCurrentInputEditorInfo()) && connection == ime.getCurrentInputConnection(); }
    private void show(AlertDialog d) {
        if (dialog != null) dialog.dismiss(); dialog = d;
        Window w = d.getWindow();
        if (root == null || root.getWindowToken() == null || w == null) { dialog = null; return; }
        WindowManager.LayoutParams p = w.getAttributes(); p.token = root.getWindowToken(); p.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG; w.setAttributes(p);
        w.addFlags(WindowManager.LayoutParams.FLAG_SECURE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        d.setOnCancelListener(x -> reset());
        try { d.show(); } catch (WindowManager.BadTokenException e) { reset(); }
    }
    public void dictate() {
        reset(); if (!safe()) return;
        if (ime.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { toast("Permití el micrófono en Ajustes IA y volvé a pulsar Voz"); settings(); return; }
        if (SpeechRecognizer.isOnDeviceRecognitionAvailable(ime)) { startVoice(true); return; }
        if (!SpeechRecognizer.isRecognitionAvailable(ime)) { toast("No hay un servicio de voz instalado. Podés escribir y corregir una selección."); return; }
        show(new AlertDialog.Builder(ime).setTitle("Dictado del dispositivo")
            .setMessage("No hay reconocimiento local disponible. El proveedor de voz de Android puede enviar audio a sus servidores y usar internet. ¿Usar este proveedor para este dictado?")
            .setPositiveButton("Usar proveedor", (d,w) -> startVoice(false)).setNegativeButton("Cancelar",(d,w) -> reset()).create());
    }
    private void startVoice(boolean local) {
        if (!safe()) return;
        final int token = generation; final InputConnection connection = ime.getCurrentInputConnection();
        try {
            recognizer = local ? SpeechRecognizer.createOnDeviceSpeechRecognizer(ime) : SpeechRecognizer.createSpeechRecognizer(ime);
            TextView status = new TextView(ime); status.setPadding(24,24,24,24); status.setText("Preparando micrófono…");
            AlertDialog listen = new AlertDialog.Builder(ime).setTitle(local ? "Dictado local · es-NI" : "Dictado del proveedor · es-NI").setView(status)
                .setPositiveButton("Terminar",null).setNegativeButton("Cancelar",(d,w) -> reset()).create();
            show(listen);
            if (!listen.isShowing()) return;
            listen.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> { if (recognizer != null) recognizer.stopListening(); status.setText("Finalizando…"); });
            recognizer.setRecognitionListener(new RecognitionListener() {
                public void onReadyForSpeech(Bundle b) { if (valid(token,connection)) status.setText("Escuchando… Hablá con naturalidad"); }
                public void onBeginningOfSpeech() {} public void onRmsChanged(float v) {} public void onBufferReceived(byte[] b) {}
                public void onEndOfSpeech() { status.setText("Procesando…"); }
                public void onError(int error) { if (!valid(token,connection)) return; reset(); toast("No se pudo reconocer la voz ("+error+"). Revisá permisos, idioma y servicio de voz. No se ha insertado texto."); }
                public void onResults(Bundle b) {
                    if (!valid(token,connection)) return;
                    ArrayList<String> values = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    stopVoice(); if (values == null || values.isEmpty()) { reset(); return; }
                    // Dictation replaces only a selection captured now, with explicit preview and insertion.
                    preview(values.get(0), "", selected(connection), token, connection, true);
                }
                public void onPartialResults(Bundle b) { if (!valid(token,connection)) return; ArrayList<String> v=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION); if(v!=null&&!v.isEmpty()) status.setText(v.get(0)); }
                public void onEvent(int type, Bundle b) {}
            });
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE,"es-NI").putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);
            if (local) intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true);
            recognizer.startListening(intent);
            main.postDelayed(() -> { if (valid(token,connection) && recognizer != null) { reset(); toast("Dictado detenido tras 90 segundos"); } },90000);
        } catch (RuntimeException e) { reset(); toast("No se pudo iniciar el dictado"); }
    }
    private String selected(InputConnection c) { CharSequence text = c.getSelectedText(0); return text == null ? "" : text.toString(); }
    private int cursor(InputConnection c) {
        android.view.inputmethod.ExtractedText t = c.getExtractedText(new android.view.inputmethod.ExtractedTextRequest(),0);
        return t == null ? -1 : t.startOffset + t.selectionStart;
    }
    private void correctSelection() {
        reset(); if (!safe()) return; InputConnection c=ime.getCurrentInputConnection(); String text=selected(c);
        if (text.isBlank() || text.length()>12000) { toast("Seleccioná el texto a corregir (máximo 12000 caracteres)"); return; }
        preview(text,text,text,generation,c,false);
    }
    private void preview(String text, String original, String selection, int token, InputConnection connection, boolean dictation) {
        if (!valid(token,connection)) return;
        final int initialCursor = cursor(connection);
        LinearLayout box = new LinearLayout(ime); box.setPadding(20,12,20,12); box.setOrientation(LinearLayout.VERTICAL);
        TextView notice = new TextView(ime); notice.setText("Revisá el resultado. Corregir envía solo este texto a Google. Insertar requiere que el campo y la selección sigan iguales."); box.addView(notice);
        EditText editor = new EditText(ime); editor.setText(text); editor.setMinLines(3); editor.setMaxLines(6); editor.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO); box.addView(editor);
        Spinner mode = new Spinner(ime); mode.setAdapter(new ArrayAdapter<>(ime,android.R.layout.simple_spinner_dropdown_item,new String[]{"Corrección básica","Corrección completa","Corrección de dictado","Conservar estilo","Mejorar redacción"})); mode.setSelection(dictation?2:3); box.addView(mode);
        Button correct = new Button(ime); correct.setText("Corregir con Gemini"); box.addView(correct);
        Button restore = new Button(ime); restore.setText("Restaurar original"); box.addView(restore); restore.setOnClickListener(v -> { if(client!=null)client.cancel(); client=null; editor.setText(text); correct.setEnabled(true); });
        TextView status = new TextView(ime); box.addView(status);
        AlertDialog review = new AlertDialog.Builder(ime).setTitle("Keyva · Revisar texto").setView(box).setPositiveButton("Insertar",null).setNegativeButton("Cancelar",(d,w)->reset()).create();
        show(review);
        if (!review.isShowing()) return;
        review.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (!valid(token,connection) || !selected(connection).equals(selection) || cursor(connection)!=initialCursor) { reset(); toast("El campo cambió. Volvé a seleccionar el texto."); return; }
            String result=editor.getText().toString();
            if (result.isEmpty() || result.length()>16000) { toast("El texto está vacío o es demasiado largo"); return; }
            connection.finishComposingText(); connection.beginBatchEdit();
            boolean ok=connection.commitText(result,1); connection.endBatchEdit();
            if (ok) { undoBefore=selection; undoAfter=result; undoCursor=cursor(connection); undoConnection=connection; }
            review.dismiss(); dialog=null; if(client!=null)client.cancel(); editor.setText("");
        });
        correct.setOnClickListener(v -> {
            if (!valid(token,connection)) { reset(); return; }
            KeyStoreConfig config = new KeyStoreConfig(ime);
            if (!config.enabled()) { toast("Activá Gemini y configurá una clave nueva en Ajustes IA"); return; }
            final String input=editor.getText().toString(); final String style=mode.getSelectedItem().toString();
            if (input.isBlank() || input.length()>12000) { toast("Máximo 12000 caracteres"); return; }
            correct.setEnabled(false); status.setText("Corrigiendo… Podés cancelar sin perder el original.");
            GeminiClient request=new GeminiClient(); if(client!=null)client.cancel(); client=request;
            worker.execute(() -> {
                try { String result=request.correct(input,style,config); main.post(() -> { if(valid(token,connection)&&review.isShowing()&&client==request) { editor.setText(result); status.setText("Resultado listo. Revisalo antes de insertar."); correct.setEnabled(true); } }); }
                catch(Exception e) { main.post(() -> { if(valid(token,connection)&&review.isShowing()&&client==request) { status.setText(e instanceof IllegalStateException ? e.getMessage() : "Error de red o configuración. El original se conserva."); correct.setEnabled(true); } }); }
            });
        });
    }
    private void undo() {
        if (!safe()) return; InputConnection c=ime.getCurrentInputConnection();
        if (undoAfter==null || c!=undoConnection || undoCursor<0 || cursor(c)!=undoCursor || !selected(c).isEmpty()) { toast("No hay una corrección que se pueda deshacer aquí"); return; }
        CharSequence before=c.getTextBeforeCursor(undoAfter.length(),0);
        if (before==null || !before.toString().equals(undoAfter)) { toast("El texto cambió. No se deshará"); clearUndo(); return; }
        c.beginBatchEdit(); if(c.deleteSurroundingText(undoAfter.length(),0)) c.commitText(undoBefore,1); c.endBatchEdit(); clearUndo();
    }
    private void clearUndo() { undoBefore=null; undoAfter=null; undoConnection=null; undoCursor=-1; }
    private void stopVoice() { SpeechRecognizer r=recognizer; recognizer=null; if(r!=null) { r.cancel(); r.destroy(); } }
    public void reset() { generation++; main.removeCallbacksAndMessages(null); stopVoice(); if(client!=null) { client.cancel(); client=null; } if(dialog!=null) { dialog.dismiss(); dialog=null; } clearUndo(); }
    public void close() { reset(); worker.shutdownNow(); root=null; }
}

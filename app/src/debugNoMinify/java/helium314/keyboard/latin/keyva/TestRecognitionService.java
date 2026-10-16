// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.keyva;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionService;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;
/** Debug-only recognizer fixture. It never captures audio or performs ASR. */
public final class TestRecognitionService extends RecognitionService {
    private final Handler handler=new Handler(Looper.getMainLooper());
    @Override protected void onStartListening(Intent intent, Callback callback) {
        try { callback.readyForSpeech(new Bundle());callback.beginningOfSpeech(); }
        catch(android.os.RemoteException ignored) { }
    }
    @Override protected void onStopListening(Callback callback) {
        handler.postDelayed(()->{
            Bundle result=new Bundle();ArrayList<String> words=new ArrayList<>();words.add("vos tenes tiempo");
            result.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION,words);
            try{callback.endOfSpeech();callback.results(result);}catch(android.os.RemoteException ignored){}
        },200);
    }
    @Override protected void onCancel(Callback callback) { handler.removeCallbacksAndMessages(null); }
    @Override public void onDestroy() { handler.removeCallbacksAndMessages(null);super.onDestroy(); }
}

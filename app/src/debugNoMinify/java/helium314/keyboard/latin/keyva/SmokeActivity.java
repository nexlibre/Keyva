// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.keyva;
import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
/** Debug-only local test surface; never packaged in release builds. */
public final class SmokeActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        TextView title=new TextView(this); title.setText("Keyva test: local text only"); box.addView(title);
        if (getIntent().getBooleanExtra("storage_test",false)) {
            try {
                KeyStoreConfig config=new KeyStoreConfig(this);
                if (!config.read().isEmpty()) throw new IllegalStateException("Existing configuration: refusing test overwrite");
                String fixture="test-only-placeholder-not-a-real-key";
                config.save(fixture,"gemini-2.5-flash-lite",false);
                if (!fixture.equals(config.read())) throw new IllegalStateException("Roundtrip failed");
                if (getSharedPreferences("keyva_ai",MODE_PRIVATE).getAll().toString().contains(fixture)) throw new IllegalStateException("Plaintext persisted");
                config.clear(); if (!config.read().isEmpty() || config.enabled()) throw new IllegalStateException("Deletion failed");
                title.setText("Keystore roundtrip: PASS");
            } catch (Exception error) { title.setText("Keystore roundtrip: FAIL " + error.getClass().getSimpleName()); }
        }
        EditText input=new EditText(this); input.setHint("Test message"); input.setContentDescription("test_message"); input.setMinLines(2);
        boolean password=getIntent().getBooleanExtra("password",false);
        input.setInputType(InputType.TYPE_CLASS_TEXT | (password ? InputType.TYPE_TEXT_VARIATION_PASSWORD : InputType.TYPE_TEXT_FLAG_MULTI_LINE)); box.addView(input);
        input.setText(getIntent().getStringExtra("fixture") == null ? "" : getIntent().getStringExtra("fixture")); input.setSelection(input.length());
        if (getIntent().getBooleanExtra("private",false)) input.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING);
        Button select=new Button(this);select.setText("Select test message");box.addView(select);select.setOnClickListener(v->{input.requestFocus();input.selectAll();});
        setContentView(box);input.requestFocus();input.postDelayed(()->((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(input,InputMethodManager.SHOW_IMPLICIT),500);
    }
}

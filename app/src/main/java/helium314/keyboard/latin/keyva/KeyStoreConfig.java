// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.keyva;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class KeyStoreConfig {
    private final SharedPreferences prefs;
    private final android.os.UserManager userManager;
    private static final String ALIAS = "keyva.gemini.v1";
    public KeyStoreConfig(Context c) {
        userManager = c.getSystemService(android.os.UserManager.class);
        prefs = c.getSharedPreferences("keyva_ai", Context.MODE_PRIVATE);
    }
    private void requireUnlocked() {
        if (userManager == null || !userManager.isUserUnlocked()) throw new IllegalStateException("Desbloqueá el teléfono para usar Gemini");
    }
    private SecretKey key() throws Exception {
        requireUnlocked();
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore"); ks.load(null);
        if (ks.containsAlias(ALIAS)) return (SecretKey) ks.getKey(ALIAS, null);
        KeyGenerator gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        gen.init(new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
            .setUnlockedDeviceRequired(android.os.Build.VERSION.SDK_INT >= 35).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());
        return gen.generateKey();
    }
    public void save(String value, String model, boolean enabled) throws Exception {
        requireUnlocked();
        if (!model.matches("gemini-[a-zA-Z0-9._-]{1,80}")) throw new IllegalArgumentException("Modelo inválido");
        SharedPreferences.Editor edit = prefs.edit().putString("model", model).putBoolean("enabled", enabled);
        if (!value.isEmpty()) {
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.ENCRYPT_MODE, key());
            edit.putString("key", Base64.encodeToString(c.doFinal(value.getBytes(StandardCharsets.UTF_8)), Base64.NO_WRAP))
                .putString("iv", Base64.encodeToString(c.getIV(), Base64.NO_WRAP));
        }
        if (!edit.commit()) throw new IllegalStateException("No se pudo guardar");
    }
    public String read() throws Exception {
        requireUnlocked();
        String value = prefs.getString("key", ""); if (value.isEmpty()) return "";
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, Base64.decode(prefs.getString("iv", ""), Base64.NO_WRAP)));
        return new String(c.doFinal(Base64.decode(value, Base64.NO_WRAP)), StandardCharsets.UTF_8);
    }
    public String speechLanguage() { return prefs.getString("speech_language", "es-NI"); }
    public void speechLanguage(String language) {
        if (!java.util.Arrays.asList("es-NI", "es-US", "es-ES", "en-US").contains(language)) throw new IllegalArgumentException("Idioma inválido");
        prefs.edit().putString("speech_language", language).apply();
    }
    public String model() { return prefs.getString("model", "gemini-2.5-flash-lite"); }
    public boolean enabled() { return prefs.getBoolean("enabled", false); }
    public void clear() throws Exception {
        prefs.edit().clear().commit(); KeyStore ks = KeyStore.getInstance("AndroidKeyStore"); ks.load(null); ks.deleteEntry(ALIAS);
    }
}

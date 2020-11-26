package de.julianhofmann.h_bank.util;

import android.content.SharedPreferences;
import android.util.Log;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import kotlin.text.Charsets;

public class PasswordCache {
    public static void storePassword(String password, SharedPreferences sp) {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            SecureRandom secureRandom = new SecureRandom();
            byte[] salt = new byte[32];

            secureRandom.nextBytes(salt);

            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 10000, 256);
            SecretKey secretKey = keyFactory.generateSecret(keySpec);

            SharedPreferences.Editor edit = sp.edit();
            edit.putString("salt", new String(salt, Charsets.ISO_8859_1));
            edit.putString("password_hash", new String(secretKey.getEncoded(), Charsets.ISO_8859_1));
            edit.apply();
        } catch (InvalidKeySpecException e) {
            Log.e("ERROR", "Cannot generate password hash: Invalid key spec!");
        } catch (NoSuchAlgorithmException ignored) {
        }
    }

    public static String checkPassword(String name, String password, SharedPreferences sp) {

        if (!sp.getString("name", "").equals(name)) return null;

        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            String saltStr = sp.getString("salt", null);

            if (saltStr != null) {

                byte[] salt = saltStr.getBytes(Charsets.ISO_8859_1);

                KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 10000, 256);
                SecretKey secretKey = keyFactory.generateSecret(keySpec);

                String passwordHash = sp.getString("password_hash", null);

                if (passwordHash != null && new String(secretKey.getEncoded(), Charsets.ISO_8859_1).equals(passwordHash)) {
                    return sp.getString("token", null);

                }
            }
        } catch (InvalidKeySpecException | NoSuchAlgorithmException ignored) {
        }

        return null;
    }

    public static void clearPassword(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.remove("salt");
        edit.remove("password_hash");
        edit.apply();

    }
}
package fr.lachemoilagrappe.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages the Room database encryption key using Android Keystore.
 *
 * On first launch, generates a random 256-bit passphrase and encrypts it
 * with a hardware-backed Keystore key. The encrypted passphrase is stored
 * in SharedPreferences. On subsequent launches, decrypts the passphrase.
 */
object DatabaseKeyHelper {

    private const val KEYSTORE_ALIAS = "lmlg_db_key"
    private const val PREFS_NAME = "lmlg_db_prefs"
    private const val PREF_ENCRYPTED_KEY = "encrypted_db_key"
    private const val PREF_KEY_IV = "db_key_iv"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val GCM_TAG_LENGTH = 128

    fun getPassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encryptedKeyB64 = prefs.getString(PREF_ENCRYPTED_KEY, null)
        val ivB64 = prefs.getString(PREF_KEY_IV, null)

        return if (encryptedKeyB64 != null && ivB64 != null) {
            decryptPassphrase(
                Base64.decode(encryptedKeyB64, Base64.NO_WRAP),
                Base64.decode(ivB64, Base64.NO_WRAP)
            )
        } else {
            generateAndStorePassphrase(prefs)
        }
    }

    private fun generateAndStorePassphrase(
        prefs: android.content.SharedPreferences
    ): ByteArray {
        // Generate a random 32-byte passphrase
        val passphrase = ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }

        // Ensure Keystore key exists
        ensureKeystoreKey()

        // Encrypt the passphrase
        val keystoreKey = getKeystoreKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keystoreKey)

        val encryptedPassphrase = cipher.doFinal(passphrase)
        val iv = cipher.iv

        // Store encrypted passphrase and IV
        prefs.edit()
            .putString(PREF_ENCRYPTED_KEY, Base64.encodeToString(encryptedPassphrase, Base64.NO_WRAP))
            .putString(PREF_KEY_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
            .apply()

        return passphrase
    }

    private fun decryptPassphrase(encryptedPassphrase: ByteArray, iv: ByteArray): ByteArray {
        val keystoreKey = getKeystoreKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, keystoreKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(encryptedPassphrase)
    }

    private fun ensureKeystoreKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
            )
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            keyGenerator.generateKey()
        }
    }

    private fun getKeystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
    }
}

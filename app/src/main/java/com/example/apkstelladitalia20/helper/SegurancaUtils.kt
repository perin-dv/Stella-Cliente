package com.example.apkstelladitalia20.helper

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SegurancaUtils {

    fun solicitarAutenticacaoBiometrica(
        activity: AppCompatActivity,
        onSucesso: () -> Unit,
        onErro: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirme sua identidade")
            .setSubtitle("Use impressão digital ou reconhecimento facial")
            .setNegativeButtonText("Usar PIN")
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSucesso()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        onErro() // PIN fallback
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(activity, "Autenticação falhou", Toast.LENGTH_SHORT).show()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    fun salvarPIN(context: Context, pin: String) {
        val prefs = EncryptedSharedPreferences.create(
            "seguranca_prefs",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        prefs.edit().putString("pin_usuario", pin).apply()
    }

    fun obterPIN(context: Context): String? {
        val prefs = EncryptedSharedPreferences.create(
            "seguranca_prefs",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return prefs.getString("pin_usuario", null)
    }
}

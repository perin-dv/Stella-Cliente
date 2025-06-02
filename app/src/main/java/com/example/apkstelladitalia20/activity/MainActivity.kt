package com.example.apkstelladitalia20

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.apkstelladitalia20.activity.HomeActivity
import com.example.apkstelladitalia20.activity.LoginActivity
import com.example.apkstelladitalia20.databinding.ActivitySplashBinding
import com.example.apkstelladitalia20.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aguarda a animação carregar 1.5s e verifica se já existe login salvo
        Handler(Looper.getMainLooper()).postDelayed({
            lifecycleScope.launch {
                val usuarioLogado = verificarLoginSalvo()
                withContext(Dispatchers.Main) {
                    if (usuarioLogado) {
                        startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                    } else {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    }
                    finish() // Encerra a MainActivity após redirecionamento
                }
            }
        }, 1500) // Tempo da splash ou animação
    }

    private suspend fun verificarLoginSalvo(): Boolean {
        val prefs = getSharedPreferences("appStella", MODE_PRIVATE)
        return prefs.contains("uidCliente")
    }


}

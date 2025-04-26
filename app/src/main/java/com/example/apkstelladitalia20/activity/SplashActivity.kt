package com.example.apkstelladitalia20.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.R


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.imageViewStellablack)
        val anim = AnimationUtils.loadAnimation(this, R.anim.logo_entrada)
        logo.startAnimation(anim)

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("clientePrefs", Context.MODE_PRIVATE)
            val nome = prefs.getString("nome", null)

            if (nome != null) {
                // Usuário já está logado
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("nomeCliente", nome)
                startActivity(intent)
            } else {
                // Usuário não logado, vai pro login
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 3000)
    }
}

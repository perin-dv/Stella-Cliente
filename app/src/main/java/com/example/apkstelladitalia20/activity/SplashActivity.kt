package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.apkstelladitalia20.R
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logo)
        val animation = AnimationUtils.loadAnimation(this, R.anim.logo_entrada)
        logo.startAnimation(animation)

        // 👉 LOGIN ANÔNIMO para garantir FirebaseAuth.uid
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
            Handler(Looper.getMainLooper()).postDelayed({
                val prefs = getSharedPreferences("appStella", MODE_PRIVATE)
                val logado = prefs.contains("uidCliente")

                if (logado) {
                    startActivity(Intent(this, HomeActivity::class.java))
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                finish()
            }, 2000)
        }
    }
}

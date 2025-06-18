package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.R
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 🌀 Animação restaurada
        val logo = findViewById<ImageView>(R.id.logo)
        val animacao = AnimationUtils.loadAnimation(this, R.anim.logo_entrada)
        logo.startAnimation(animacao)

        prefs = getSharedPreferences("appStella", MODE_PRIVATE)
        auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({

            val email = prefs.getString("emailCliente", null)
            val senha = prefs.getString("senhaCliente", null)

            if (!email.isNullOrEmpty() && !senha.isNullOrEmpty()) {
                auth.signInWithEmailAndPassword(email, senha)
                    .addOnSuccessListener {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

        }, 2000)
    }
}

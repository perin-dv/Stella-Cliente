package com.example.apkstelladitalia20.activity

import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.R

class SegurancaActivity : AppCompatActivity() {

    private lateinit var switchBiometria: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seguranca)

        // Toolbar com botão de voltar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        switchBiometria = findViewById(R.id.switchBiometria)

        // Carregar preferência
        val prefs = getSharedPreferences("appStella", MODE_PRIVATE)
        switchBiometria.isChecked = prefs.getBoolean("biometria_habilitada", false)

        // Listener para salvar preferência
        switchBiometria.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("biometria_habilitada", isChecked).apply()
        }
    }


}

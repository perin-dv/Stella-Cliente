package com.example.apkstelladitalia20.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.R

class CartoesCadastradosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cartoes_cadastrados)

        findViewById<ImageView>(R.id.btnVoltar).setOnClickListener {
            finish()
        }

        val listaCartoes = listOf("••• 9636", "••• 4521")
        val container = findViewById<LinearLayout>(R.id.containerCartoes)

        for (numero in listaCartoes) {
            val view = layoutInflater.inflate(R.layout.item_cartao_salvo, container, false)
            view.findViewById<TextView>(R.id.tvNumeroCartao).text = "Cartão $numero"
            container.addView(view)
        }
    }
}

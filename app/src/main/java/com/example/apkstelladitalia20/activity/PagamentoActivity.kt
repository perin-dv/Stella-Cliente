package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.bottom.CartaoBottomSheet

class PagamentoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagamento)

        // Botões da tela
        val btnNovoCartao = findViewById<LinearLayout>(R.id.cardCadastrar)
        val btnCartaoSalvo = findViewById<LinearLayout>(R.id.cardCartaoSalvo)
        val btnVerTodasTransacoes = findViewById<LinearLayout>(R.id.cardVerTodos)
        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)

        // Ação: Cadastrar novo cartão
        btnNovoCartao.setOnClickListener {
            startActivity(Intent(this, NovoCartaoActivity::class.java))
        }

        // Ação: Detalhes do cartão salvo (abre BottomSheet)
        btnCartaoSalvo.setOnClickListener {
            CartaoBottomSheet("9636", "Mastercard").show(supportFragmentManager, "cartao_bottomsheet")
        }

        // Ação: Ver todas as transações (abre nova tela)
        btnVerTodasTransacoes.setOnClickListener {
            startActivity(Intent(this, HistoricoTransacoesActivity::class.java))
        }

        // Voltar
        btnVoltar.setOnClickListener {
            finish()
        }
    }
}

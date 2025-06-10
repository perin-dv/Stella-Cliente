package com.example.apkstelladitalia20.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.Entity.TransacaoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.adpter.HistoricoCartaoAdapter

class HistoricoTransacoesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagamento)

        val recycler = findViewById<RecyclerView>(R.id.recyclerHistorico)
        recycler.layoutManager = LinearLayoutManager(this)

        val listaExemplo = listOf(
            TransacaoEntity(86.99, "9636", "12/04/2025 às 20:48"),
            TransacaoEntity(84.99, "9636", "18/05/2025 às 19:46"),
            TransacaoEntity(65.00, "9636", "21/02/2025 às 21:12")
        )

        recycler.adapter = HistoricoCartaoAdapter(listaExemplo)
    }
}

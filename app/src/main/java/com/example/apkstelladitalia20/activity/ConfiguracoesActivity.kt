package com.example.apkstelladitalia20.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.R
import com.google.firebase.auth.FirebaseAuth

class ConfiguracoesActivity : AppCompatActivity() {

    private val opcoes = listOf(
        "Limpar histórico de busca",
        "Sobre esta versão",
        "Sair"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracoes)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarConfiguracoes)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val listView = findViewById<ListView>(R.id.listViewConfiguracoes)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, opcoes)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> limparHistoricoBusca()
                1 -> mostrarSobreVersao()
                2 -> realizarLogout()
            }
        }
    }

    private fun limparHistoricoBusca() {
        Toast.makeText(this, "Histórico limpo com sucesso", Toast.LENGTH_SHORT).show()
        // Aqui você pode limpar localmente: SharedPreferences, banco, etc.
    }

    private fun mostrarSobreVersao() {
        AlertDialog.Builder(this)
            .setTitle("Sobre esta versão")
            .setMessage("App Stella D’Italia\nVersão 1.0.0\nÚltima atualização: Junho/2025")
            .setPositiveButton("Fechar", null)
            .show()
    }

    private fun realizarLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.databinding.ActivityCardapioPorCategoriBinding
import com.example.stelladitaliaempresa.data.AppDatabase
import com.example.stelladitaliaempresa.data.ProdutoDao
import com.stelladitalia.adapters.ProdutoAdapter


class CardapioPorCategoriaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCardapioPorCategoriBinding
    private lateinit var produtoAdapter: ProdutoAdapter
    private lateinit var produtoDao: ProdutoDao
    private lateinit var categoriaSelecionada: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardapioPorCategoriBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoriaSelecionada = intent.getStringExtra("categoria") ?: ""

        binding.tituloCategoria.text = categoriaSelecionada

        val dao: ProdutoDao = AppDatabase.getDatabase(this).produtoDao()
        produtoAdapter = ProdutoAdapter(this,emptyList()) { produto ->
            val intent = Intent(this, DetalhesProdutoActivity::class.java)
            intent.putExtra("produtoId", produto.id)
            startActivity(intent)
        }

        binding.recyclerProdutos.adapter = produtoAdapter
        binding.recyclerProdutos.layoutManager = LinearLayoutManager(this)

        carregarProdutos()
    }

    private fun carregarProdutos() {
        val produtos = produtoDao.buscarPorCategoria(categoriaSelecionada)
        produtoAdapter.atualizarLista(produtos)
    }
}

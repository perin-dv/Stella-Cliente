package com.example.apkstelladitalia20.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.apkstelladitalia20.adapter.AdicionalAdapter
import com.example.apkstelladitalia20.databinding.ActivityDetalhesProdutoBinding
import com.example.apkstelladitalia20.model.Adicional
import com.example.stelladitaliaempresa.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalhesProdutoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesProdutoBinding
    private var precoBase: Double = 0.0
    private val adicionaisSelecionados = mutableListOf<Adicional>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesProdutoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val produtoId = intent.getStringExtra("produtoId")
        val dao = AppDatabase.getDatabase(this).produtoDao()

        // Busca produto por ID
        lifecycleScope.launch {
            val produto = withContext(Dispatchers.IO) {
                dao.buscarPorId(produtoId ?: "")
            }

            produto?.let {
                precoBase = it.precoAtual
                binding.nomeProduto.text = it.nome
                binding.descricaoProduto.text = it.descricao
                binding.precoProduto.text = "R$ ${String.format("%.2f", it.precoAtual)}"


                atualizarPrecoTotal()
            }
        }

        carregarAdicionais()
    }

    private fun carregarAdicionais() {
        // Lista de exemplo - futuramente pode vir do Firebase
        val listaAdicionais = listOf(
            Adicional("1", "Refrigerante 350ml", 4.99, "https://i.imgur.com/1.png"),
            Adicional("2", "Água sem gás", 2.50, "https://i.imgur.com/2.png"),
            Adicional("3", "Suco Natural", 5.99, "https://i.imgur.com/3.png")
        )

        val adapter = AdicionalAdapter(listaAdicionais) { adicional ->
            if (adicionaisSelecionados.contains(adicional)) {
                adicionaisSelecionados.remove(adicional)
            } else {
                adicionaisSelecionados.add(adicional)
            }
            atualizarPrecoTotal()
        }

        binding.recyclerAdicionais.adapter = adapter
    }

    private fun atualizarPrecoTotal() {
        val precoTotal = precoBase + adicionaisSelecionados.sumOf { it.preco }
        binding.tituloAdicionais.text = "Adicionar - R$ ${String.format("%.2f", precoTotal)}"
    }
}

package com.example.apkstelladitalia20.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.adapter.AdicionaisAdapter
import com.example.apkstelladitalia20.databinding.ActivityDetalhesProdutoBinding
import com.example.apkstelladitalia20.helper.setupToolbar
import com.example.apkstelladitalia20.model.Adicional
import com.example.stelladitaliaempresa.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalhesProdutoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesProdutoBinding
    private var precoBase: Double = 0.0
    private val adicionaisSelecionadosProduto = mutableListOf<ProdutoEntity>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesProdutoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.includeToolbar)

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
        val listaAdicionais = listOf(
            ProdutoEntity(id = "1", nome = "Refrigerante 350ml", valor = 4.99, imagemBase64 = ""),
            ProdutoEntity(id = "2", nome = "Água sem gás", valor = 2.50, imagemBase64 = ""),
            ProdutoEntity(id = "3", nome = "Suco Natural", valor = 5.99, imagemBase64 = "")
        )

        val adapter = AdicionaisAdapter(listaAdicionais, adicionaisSelecionadosProduto) { adicional ->
            if (adicionaisSelecionadosProduto.contains(adicional)) {
                adicionaisSelecionadosProduto.remove(adicional)
            } else {
                adicionaisSelecionadosProduto.add(adicional)
            }
            atualizarPrecoTotal()
        }

        binding.recyclerAdicionais.adapter = adapter
    }


    private fun atualizarPrecoTotal() {
        val precoTotal = precoBase + adicionaisSelecionadosProduto.sumOf { it.valor }
        binding.tituloAdicionais.text = "Adicionar - R$ ${String.format("%.2f", precoTotal)}"
    }
}

package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.adapter.AdicionaisAdapter
import com.example.apkstelladitalia20.databinding.ActivityDetalhesProdutoBinding
import com.example.apkstelladitalia20.helper.setupToolbar
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlin.jvm.java

class DetalhesProdutoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesProdutoBinding
    private var precoBase: Double = 0.0
    private val adicionaisSelecionadosProduto = mutableListOf<ProdutoEntity>()
    private var quantidade = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesProdutoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.includeToolbar)



        setupBotoesQuantidade()


        binding.btnConfirmarPedido.setOnClickListener {
            val intent = Intent(this, CarrinhoActivity::class.java)
            Toast.makeText(this, "Adicionado ao carrinho ✅", Toast.LENGTH_SHORT).show()
            finish()
            startActivity(intent)
        }

        val produtoId = intent.getStringExtra("produtoId")
        if (produtoId.isNullOrBlank()) {
            Toast.makeText(this, "Produto não encontrado!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            val uidEmpresa = getSharedPreferences("appStella", MODE_PRIVATE)
                .getString("uidEmpresa", "") ?: return@launch

            val ref = FirebaseDatabase.getInstance()
                .getReference("empresa")
                .child(uidEmpresa)
                .child("produtos")
                .child(produtoId)

            ref.get().addOnSuccessListener { snapshot ->
                val produto = snapshot.getValue(ProdutoEntity::class.java)
                produto?.let {
                    precoBase = it.valor
                    binding.nomeProduto.text = it.nome
                    binding.descricaoProduto.text = it.descricao
                    binding.precoProduto.text = "R$ %.2f".format(it.valor)

                    if (!it.imagem.isNullOrBlank()) {
                        try {
                            val base64 = it.imagem.replace("\\s".toRegex(), "")
                            val imagemBytes = Base64.decode(base64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imagemBytes, 0, imagemBytes.size)
                            binding.imagemProduto.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    carregarAdicionais()
                    atualizarPrecoTotal()
                }
            }
        }
    }

    private fun setupBotoesQuantidade() {
        binding.btnAdicionar.setOnClickListener {
            quantidade++
            binding.quantidadeProduto.text = quantidade.toString()
            atualizarPrecoTotal()
        }

        binding.btnRemover.setOnClickListener {
            if (quantidade > 1) {
                quantidade--
                binding.quantidadeProduto.text = quantidade.toString()
                atualizarPrecoTotal()
            }
        }
    }

    private fun carregarAdicionais() {
        val uidEmpresa = getSharedPreferences("appStella", MODE_PRIVATE)
            .getString("uidEmpresa", "") ?: return
        val ref = FirebaseDatabase.getInstance()
            .getReference("empresa")
            .child(uidEmpresa)
            .child("produtos")

        ref.get().addOnSuccessListener { snapshot ->
            val adicionais = mutableListOf<ProdutoEntity>()
            for (item in snapshot.children) {
                val produto = item.getValue(ProdutoEntity::class.java)
                if (produto?.categoria?.contains("bebida", ignoreCase = true) == true) {
                    adicionais.add(produto)
                }
            }

            val adapter = AdicionaisAdapter(adicionais, adicionaisSelecionadosProduto) { adicional ->
                if (adicionaisSelecionadosProduto.contains(adicional)) {
                    adicionaisSelecionadosProduto.remove(adicional)
                } else {
                    adicionaisSelecionadosProduto.add(adicional)
                }
                atualizarPrecoTotal()
            }

            binding.recyclerAdicionais.layoutManager = LinearLayoutManager(this)
            binding.recyclerAdicionais.adapter = adapter
        }
    }

    private fun atualizarPrecoTotal() {
        val precoExtras = adicionaisSelecionadosProduto.sumOf { it.valor }
        val precoFinal = precoBase + precoExtras
        binding.precoTotal.text = "R$ %.2f".format(precoFinal)
    }
}

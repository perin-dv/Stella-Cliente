package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.MainActivity
import com.example.apkstelladitalia20.adapter.AdicionaisAdapter

import com.example.apkstelladitalia20.databinding.ActivityDetalhesProdutoBinding
import com.example.apkstelladitalia20.helper.setupToolbar
import com.example.apkstelladitalia20.model.CarrinhoViewModel
import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class DetalhesProdutoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesProdutoBinding
    private var precoBase: Double = 0.0
    private var produtoCategoria: String? = null
    private val adicionaisSelecionadosProduto = mutableListOf<ProdutoEntity>()
    private var quantidade = 1
    private var produtoSelecionado: ProdutoEntity? = null
    private lateinit var viewModel: CarrinhoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesProdutoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.includeToolbar)


        viewModel = ViewModelProvider(this)[CarrinhoViewModel::class.java]





        binding.btnConfirmarPedido.setOnClickListener {
            adicionarAoCarrinhoEIrParaCarrinho()

            setupBotoesQuantidade()
        }

        val produtoId = intent.getStringExtra("produtoId")
        if (!produtoId.isNullOrEmpty()) {
            carregarProduto(produtoId)
        }
    }

    private fun setupBotoesQuantidade() {
        binding.btnRemover.setOnClickListener {
            if (quantidade > 1) {
                quantidade--
                binding.quantidadeProduto.text = quantidade.toString()
                atualizarPrecoTotal()
            }
        }

        binding.btnAdicionar.setOnClickListener {
            quantidade++
            binding.quantidadeProduto.text = quantidade.toString()
            atualizarPrecoTotal()
        }
    }


    private fun adicionarAoCarrinhoEIrParaCarrinho() {
        produtoSelecionado?.let { produto ->
            val total = (precoBase * quantidade) + adicionaisSelecionadosProduto.sumOf { it.valor }
            val item = ProdutoCarrinhoEntity(
                idProduto = produto.id,
                nome = produto.nome,
                valor = total,
                quantidade = quantidade,
                tipo = "produto",
                descricao = produto.descricao,
                imagemUrl = produto.imagem,
                categoria = produto.categoria
            )
            viewModel.adicionar(item)


            adicionaisSelecionadosProduto.forEach { adicional ->
                val adicionalItem = ProdutoCarrinhoEntity(
                    idProduto = adicional.id,
                    nome = adicional.nome,
                    valor = adicional.getPrecoReal(),
                    quantidade = 1,
                    tipo = "produto",
                    descricao = adicional.descricao,
                    imagemUrl = adicional.imagem,
                    categoria = adicional.categoria
                )
                viewModel.adicionar(adicionalItem)
            }


            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("abrirCarrinho", true)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun carregarProduto(produtoId: String) {
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
                    produtoSelecionado = it
                    precoBase = it.getPrecoReal()
                    produtoCategoria = it.categoria
                    binding.nomeProduto.text = it.nome
                    binding.descricaoProduto.text = it.descricao
                    binding.precoProduto.text = "R$ %.2f".format(precoBase)

                    if (!it.imagem.isNullOrBlank()) {
                        try {
                            val base64 = it.imagem.replace("\\s".toRegex(), "")
                            val imagemBytes = Base64.decode(base64, Base64.DEFAULT)
                            val bitmap =
                                BitmapFactory.decodeByteArray(imagemBytes, 0, imagemBytes.size)
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

    private fun carregarAdicionais() {
        val uidEmpresa = getSharedPreferences("appStella", MODE_PRIVATE)
            .getString("uidEmpresa", "") ?: return

        val ref = FirebaseDatabase.getInstance()
            .getReference("empresa")
            .child(uidEmpresa)
            .child("produtos")

        ref.get().addOnSuccessListener { snapshot ->
            val produtos = mutableListOf<ProdutoEntity>()
            val categoriaDesejada = if (produtoCategoria?.contains("bebida", true) == true)
                "pizza" else "bebida"

            for (item in snapshot.children) {
                val produto = item.getValue(ProdutoEntity::class.java)
                if (produto?.categoria?.contains(categoriaDesejada, true) == true) {
                    produtos.add(produto)
                }
            }

            val agrupados = produtos.groupBy { it.categoria ?: "Outros" }
            val listaFinal = mutableListOf<ProdutoEntity>()

            agrupados.forEach { (categoria, itens) ->
                listaFinal.add(ProdutoEntity().apply {
                    id = "titulo"
                    descricao = categoria
                })
                listaFinal.addAll(itens)
            }

            val adapter =
                AdicionaisAdapter(listaFinal, adicionaisSelecionadosProduto) { adicional ->
                    adicionaisSelecionadosProduto.add(adicional)
                    adicionarAoCarrinhoEIrParaCarrinho()
                }

            binding.recyclerAdicionais.layoutManager = LinearLayoutManager(this)
            binding.recyclerAdicionais.adapter = adapter
        }
    }

    private fun atualizarPrecoTotal() {
        val precoExtras = adicionaisSelecionadosProduto.sumOf { it.valor }
        val precoFinal = (precoBase * quantidade) + precoExtras
        binding.precoTotal.text = "R$ %.2f".format(precoFinal)
    }
}

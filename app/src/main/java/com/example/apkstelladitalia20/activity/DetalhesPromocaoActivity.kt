package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.adpter.ProdutoInclusoAdapter
import com.example.apkstelladitalia20.databinding.ActivityDetalhesPromocaoBinding
import com.example.apkstelladitalia20.helper.setupToolbar
import com.example.apkstelladitalia20.model.CarrinhoViewModel
import com.example.apkstelladitalia20.model.PromocaoEntity
import com.google.firebase.database.FirebaseDatabase

class DetalhesPromocaoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesPromocaoBinding
    private var promocaoAtual: PromocaoEntity? = null

    private var quantidade = 1
    private var precoUnitario = 0.0
    private lateinit var carrinhoViewModel: CarrinhoViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesPromocaoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.includeToolbar)

        // ✅ Correção aqui: ViewModelProvider(this) em vez de applicationContext
        carrinhoViewModel = ViewModelProvider(this)[CarrinhoViewModel::class.java]


        promocaoAtual = intent.getParcelableExtra("promocaoSelecionada")
        if (promocaoAtual == null) {
            Toast.makeText(this, "Erro ao carregar promoção!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        exibirDados(promocaoAtual!!)
        buscarProdutosIncluidos()
        setupQuantidadeButtons()
        setupBotaoAdicionar()
    }

    private fun exibirDados(promocao: PromocaoEntity) {
        binding.nomeProduto.text = promocao.titulo.ifEmpty { "Promoção Especial" }
        binding.descricaoProduto.text = promocao.observacao.ifEmpty { "" }

        if (!promocao.imagemBase64.isNullOrEmpty()) {
            val imagemBytes = Base64.decode(promocao.imagemBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imagemBytes, 0, imagemBytes.size)
            binding.imagemProduto.setImageBitmap(bitmap)
        }

        val produtosInclusos = promocao.produtos ?: emptyList()
        binding.recyclerAdicionais.layoutManager = LinearLayoutManager(this)
        binding.recyclerAdicionais.adapter = ProdutoInclusoAdapter(produtosInclusos)

        val valorOriginal = produtosInclusos.sumOf { it.valor }
        val valorPromocional = promocao.valor.takeIf { it > 0.0 } ?: valorOriginal

        binding.precoProduto.text = "R$ %.2f".format(valorPromocional)
        precoUnitario = valorPromocional

        if (valorOriginal > 0 && valorPromocional > 0) {
            val percentualDesconto = ((valorOriginal - valorPromocional) / valorOriginal) * 100
            binding.txtDescontoPromocao.text =
                "🔥 Economize %d%%!\nDe R$ %.2f por R$ %.2f".format(
                    percentualDesconto.toInt(),
                    valorOriginal,
                    valorPromocional
                )

        } else {
            binding.txtDescontoPromocao.text = ""
        }

        atualizarPrecoTotal()
    }

    private fun setupQuantidadeButtons() {
        binding.btnAdicionar.setOnClickListener {
            quantidade++
            atualizarQuantidade()
        }

        binding.btnRemover.setOnClickListener {
            if (quantidade > 1) {
                quantidade--
                atualizarQuantidade()
            }
        }
    }

    private fun atualizarQuantidade() {
        binding.quantidadeProduto.text = quantidade.toString()
        atualizarPrecoTotal()
    }

    private fun atualizarPrecoTotal() {
        val precoTotal = precoUnitario * quantidade
        binding.precoTotal.text = "R$ %.2f".format(precoTotal)
    }

    private fun setupBotaoAdicionar() {
        binding.btnConfirmarPedido.setOnClickListener {
            promocaoAtual?.let { promocao ->
                val valorPromocional = promocao.valor.takeIf { it > 0.0 }
                    ?: promocao.produtos?.sumOf { it.valor } ?: 0.0

                val item = ProdutoCarrinhoEntity(
                    idProduto = "promo_${System.currentTimeMillis()}",
                    nome = promocao.titulo.ifEmpty { "Promoção Especial" },
                    valor = valorPromocional * quantidade,
                    quantidade = quantidade,
                    tipo = "promocao",
                    descricao = promocao.observacao,
                    imagemUrl = promocao.imagemBase64
                )

                carrinhoViewModel.salvarPromocao(promocaoAtual!!)


                Toast.makeText(this, "Promoção adicionada ao carrinho ✅", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("abrirCarrinho", true)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
        }
    }

    private fun buscarProdutosIncluidos() {
        val produtosParaBuscar = promocaoAtual?.produtos ?: return
        val listaProdutos = mutableListOf<ProdutoEntity>()

        if (produtosParaBuscar.isEmpty()) {
            exibirDados(promocaoAtual!!)
            return
        }

        val idUsuario = promocaoAtual?.idUsuario ?: return
        val databaseRef = FirebaseDatabase.getInstance()
            .getReference("empresa")
            .child(idUsuario)
            .child("produtos")

        var produtosBuscados = 0
        for (produto in produtosParaBuscar) {
            databaseRef.child(produto.id).get().addOnSuccessListener { snapshot ->
                val produtoCompleto = snapshot.getValue(ProdutoEntity::class.java)
                produtoCompleto?.let {
                    val produtoComValor = it.copy(valor = produto.valor)
                    listaProdutos.add(produtoComValor)
                }

                produtosBuscados++
                if (produtosBuscados == produtosParaBuscar.size) {
                    promocaoAtual?.produtos = listaProdutos
                    exibirDados(promocaoAtual!!)
                }
            }.addOnFailureListener {
                produtosBuscados++
                if (produtosBuscados == produtosParaBuscar.size) {
                    promocaoAtual?.produtos = listaProdutos
                    exibirDados(promocaoAtual!!)
                }
            }
        }

    }

}

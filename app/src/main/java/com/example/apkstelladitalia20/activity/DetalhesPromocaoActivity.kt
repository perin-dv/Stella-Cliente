package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.adapter.AdicionaisAdapter
import com.example.apkstelladitalia20.adpter.ProdutoInclusoAdapter
import com.example.apkstelladitalia20.databinding.ActivityDetalhesPromocaoBinding
import com.example.apkstelladitalia20.helper.CarrinhoController
import com.example.apkstelladitalia20.helper.setupToolbar
import com.example.apkstelladitalia20.model.PromocaoEntity
import com.google.firebase.database.FirebaseDatabase

class DetalhesPromocaoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesPromocaoBinding
    private val adicionaisSelecionadosPromocao = mutableListOf<ProdutoEntity>()
    private var promocaoAtual: PromocaoEntity? = null
    private lateinit var promocao: PromocaoEntity

    private var quantidade = 1
    private var precoUnitario = 0.0 // preço de 1 item
    private val idEmpresa = "7a3118oNdgcpmwSqrgyRTqBnFFx2" // substitui pelo seu ID correto


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesPromocaoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.includeToolbar)


        promocaoAtual = intent.getParcelableExtra("promocaoSelecionada")
        if (promocaoAtual == null) {
            Toast.makeText(this, "Erro ao carregar promoção!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        buscarProdutosIncluidos()
        setupQuantidadeButtons()
        setupBotaoAdicionar()
    }

    private fun exibirDados(promocao: PromocaoEntity) {
        binding.nomeProduto.text = promocao.titulo
        binding.descricaoProduto.text = promocao.descricao

        if (!promocao.imagemBase64.isNullOrEmpty()) {
            val imagemBytes = Base64.decode(promocao.imagemBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imagemBytes, 0, imagemBytes.size)
            binding.imagemProduto.setImageBitmap(bitmap)
        }

        val produtos = promocao.produtos
        val valorOriginal = produtos.sumOf { it.valor }
        val valorPromocional = promocao.valor.takeIf { it > 0.0 } ?: valorOriginal

        if (valorOriginal > 0 && valorPromocional > 0) {
            val percentualDesconto = ((valorOriginal - valorPromocional) / valorOriginal) * 100
            binding.txtDescontoPromocao.text =
                "🔥 Economize ${percentualDesconto.toInt()}%!\nDe R$ %.2f por R$ %.2f".format(
                    valorOriginal,
                    valorPromocional
                )
        } else {
            binding.txtDescontoPromocao.text = ""
        }

        precoUnitario = valorPromocional
        binding.precoProduto.text = "R$ %.2f".format(precoUnitario)
        atualizarPrecoTotal()

        binding.recyclerAdicionais.layoutManager = LinearLayoutManager(this)
        binding.recyclerAdicionais.adapter = ProdutoInclusoAdapter(produtos)
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

    private fun buscarProdutosIncluidos() {
        val produtosParaBuscar = promocaoAtual?.produtos ?: return
        val listaProdutos = mutableListOf<ProdutoEntity>()

        if (produtosParaBuscar.isEmpty()) {
            exibirDados(promocaoAtual!!)
            return
        }

        val databaseRef = FirebaseDatabase.getInstance()
            .getReference("empresa")
            .child(promocaoAtual!!.idUsuario)
            .child("produtos")

        var produtosBuscados = 0

        for (produto in produtosParaBuscar) {
            databaseRef.child(produto.id).get().addOnSuccessListener { snapshot ->
                val produtoCompleto = snapshot.getValue(ProdutoEntity::class.java)
                produtoCompleto?.let {
                    listaProdutos.add(it)
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

    private fun atualizarPrecoTotal() {
        val precoTotal = precoUnitario * quantidade
        binding.precoTotal.text = "R$ %.2f".format(precoTotal)
    }


    private fun atualizarRecyclerView(produtos: List<ProdutoEntity>) {
        binding.recyclerAdicionais.layoutManager = LinearLayoutManager(this)
        binding.recyclerAdicionais.adapter =
            AdicionaisAdapter(produtos, adicionaisSelecionadosPromocao) { adicional ->
                if (adicionaisSelecionadosPromocao.contains(adicional)) {
                    adicionaisSelecionadosPromocao.remove(adicional)
                } else {
                    adicionaisSelecionadosPromocao.add(adicional)
                }
                atualizarPrecoTotal()
            }

        // 🔥 Aqui recalculamos o desconto com os produtos completos
        atualizarDesconto()
    }

    private fun carregarProdutosDaPromocao(promocao: PromocaoEntity) {
        val listaProdutos = mutableListOf<ProdutoEntity>()
        val databaseRef = FirebaseDatabase.getInstance()
            .getReference("empresa")
            .child("7a3118oNdgcpmwSqrgyRTqBnFFx2")
            .child("produtos")

        val produtosParaBuscar = promocao.produtos
        var produtosBuscados = 0

        if (produtosParaBuscar.isEmpty()) {
            exibirDados(promocao)
            return
        }

        for (produto in produtosParaBuscar) {
            databaseRef.child(produto.id).get().addOnSuccessListener { snapshot ->
                val produtoCompleto = snapshot.getValue(ProdutoEntity::class.java)
                produtoCompleto?.let {
                    listaProdutos.add(it)
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


    private fun atualizarDesconto() {
        val produtos = promocaoAtual?.produtos ?: emptyList()
        val valorOriginal = produtos.sumOf { it.valor }
        val valorPromocional = promocaoAtual?.valor?.takeIf { it > 0.0 } ?: valorOriginal

        if (valorOriginal > 0 && valorPromocional > 0) {
            val percentualDesconto = ((valorOriginal - valorPromocional) / valorOriginal) * 100
            binding.txtDescontoPromocao.text =
                "🔥 Economize ${percentualDesconto.toInt()}%!\nDe R$ %.2f por R$ %.2f".format(
                    valorOriginal,
                    valorPromocional
                )
        } else {
            binding.txtDescontoPromocao.text = ""
        }
    }


    private fun setupBotaoAdicionar() {
        binding.btnConfirmarPedido.setOnClickListener {
            promocaoAtual?.let { promocao ->
                CarrinhoController.adicionarItem(promocao, quantidade)

                Toast.makeText(this, "Adicionado ao carrinho de compras ✅", Toast.LENGTH_SHORT)
                    .show()

                val intent = Intent(this, EnderecoEntregaActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}

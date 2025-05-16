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
    private var promocaoAtual: PromocaoEntity? = null

    private var quantidade = 1
    private var precoUnitario = 0.0 // preço de 1 item


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesPromocaoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.includeToolbar)

        promocaoAtual = intent.getParcelableExtra<PromocaoEntity>("promocaoSelecionada")
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
        // Título e descrição
        binding.nomeProduto.text = promocao.titulo.ifEmpty { "Promoção Especial" }
        binding.descricaoProduto.text = promocao.observacao.ifEmpty { "" }

        // Imagem da promoção
        if (!promocao.imagemBase64.isNullOrEmpty()) {
            val imagemBytes = Base64.decode(promocao.imagemBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imagemBytes, 0, imagemBytes.size)
            binding.imagemProduto.setImageBitmap(bitmap)
        }

        // Produtos inclusos na promoção
        val produtosInclusos = promocao.produtos ?: emptyList()

        // Exibir os produtos com imagem, nome e preço
        binding.recyclerAdicionais.layoutManager = LinearLayoutManager(this)
        binding.recyclerAdicionais.adapter = ProdutoInclusoAdapter(produtosInclusos)

        // Preços e desconto
        val valorOriginal = produtosInclusos.sumOf { it.valor }
        val valorPromocional = promocao.valor.takeIf { it > 0.0 } ?: valorOriginal

        binding.precoProduto.text = "R$ %.2f".format(valorPromocional)
        precoUnitario = valorPromocional

        if (valorOriginal > 0 && valorPromocional > 0) {
            val percentualDesconto = ((valorOriginal - valorPromocional) / valorOriginal) * 100
            binding.txtDescontoPromocao.text =
                "🔥 Economize ${percentualDesconto.toInt()}%%!\nDe R$ %.2f por R$ %.2f".format(valorOriginal, valorPromocional)

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



    private fun buscarProdutosIncluidos() {
        val produtosParaBuscar = promocaoAtual?.produtos ?: return
        val listaProdutos = mutableListOf<ProdutoEntity>()

        if (produtosParaBuscar.isEmpty()) {
            exibirDados(promocaoAtual!!)
            return
        }

        val idUsuario = promocaoAtual?.idUsuario
        if (idUsuario.isNullOrBlank()) {
            Log.e("DetalhesPromocao", "ID do usuário da promoção está vazio")
            exibirDados(promocaoAtual!!)
            return
        }

        val databaseRef = FirebaseDatabase.getInstance()
            .getReference("empresa")
            .child(idUsuario)
            .child("produtos")

        var produtosBuscados = 0

        for (produto in produtosParaBuscar) {
            databaseRef.child(produto.id).get().addOnSuccessListener { snapshot ->
                val produtoCompleto = snapshot.getValue(ProdutoEntity::class.java)
                Log.d("FIREBASE_PRODUTO", "Nome: ${produtoCompleto?.nome}, imagem: ${produtoCompleto?.imagem?.take(20)}")

                produtoCompleto?.let {
                    val produtoComValor = it.copy(valor = produto.valor) // ← aqui mantém o valor original
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


    private fun atualizarPrecoTotal() {
        val precoTotal = precoUnitario * quantidade
        binding.precoTotal.text = "R$ %.2f".format(precoTotal)
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

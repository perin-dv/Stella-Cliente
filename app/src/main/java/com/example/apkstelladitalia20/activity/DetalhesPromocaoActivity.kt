package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.databinding.ActivityDetalhesPromocaoBinding
import com.example.apkstelladitalia20.helper.CarrinhoController
import com.example.apkstelladitalia20.helper.setupToolbar
import com.example.apkstelladitalia20.model.PromocaoEntity

class DetalhesPromocaoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesPromocaoBinding

    private var quantidade = 1
    private var precoUnitario = 0.0 // preço de 1 item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesPromocaoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.includeToolbar)

        val promocao = intent.getSerializableExtra("promocaoSelecionada") as? PromocaoEntity

        if (promocao != null) {
            exibirDados(promocao)
        } else {
            finish() // se deu ruim, fecha
        }

        setupQuantidadeButtons()
        setupBotaoAdicionar()
    }

    private fun exibirDados(promocao: PromocaoEntity) {
        binding.nomeProduto.text = promocao.titulo ?: "Promoção Especial"
        binding.descricaoProduto.text = promocao.descricao ?: ""

        val produtos = promocao.produtos ?: emptyList()

        val valorOriginal = produtos.sumOf { it.valor ?: 0.0 }
        val valorPromocional = promocao.valor ?: valorOriginal

        if (valorOriginal > 0 && valorPromocional > 0) {
            val percentualDesconto = ((valorOriginal - valorPromocional) / valorOriginal) * 100
            binding.txtDescontoPromocao.text = "🔥 Economize ${percentualDesconto.toInt()}%!\nDe R$ %.2f por R$ %.2f".format(valorOriginal, valorPromocional)
        } else {
            binding.txtDescontoPromocao.text = ""
        }

        precoUnitario = valorPromocional
        binding.precoProduto.text = "R$ %.2f".format(precoUnitario)

        atualizarPrecoTotal()

        binding.recyclerAdicionais.layoutManager = LinearLayoutManager(this)
        // binding.recyclerAdicionais.adapter = AdicionaisAdapter(produtos) (já ajeitamos pra fazer certo depois)
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
            val promocao = intent.getSerializableExtra("promocaoSelecionada") as? PromocaoEntity

            if (promocao != null) {
                CarrinhoController.adicionarItem(promocao, quantidade)

                Toast.makeText(this, "Adicionado ao carrinho de compras ✅", Toast.LENGTH_SHORT).show()

                // Abrir a tela do Carrinho
                val intent = Intent(this, EnderecoEntregaActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }


}

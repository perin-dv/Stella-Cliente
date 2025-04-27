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
        // Exibir imagem da promoção
        promocao.imagemBase64?.let {
            try {
                val base64Clean = it.replace("\\s".toRegex(), "")
                val bytes = Base64.decode(base64Clean, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) {
                    binding.imagemProduto.setImageBitmap(bmp)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Nome
        binding.nomeProduto.text = promocao.titulo ?: "Promoção Especial"

        // Descrição
        binding.descricaoProduto.text = promocao.observacao ?: "Sem observações."

        // Preço
        precoUnitario = promocao.valor ?: 0.0
        binding.precoProduto.text = "R$ %.2f".format(precoUnitario)

        // Preço total inicial
        atualizarPrecoTotal()

        // RecyclerView de adicionais (caso tenha)
        binding.recyclerAdicionais.layoutManager = LinearLayoutManager(this)
        // binding.recyclerAdicionais.adapter = AdicionaisAdapter(promocao.produtos)
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

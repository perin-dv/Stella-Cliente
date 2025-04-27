package com.example.apkstelladitalia20.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.bottomsheet.BottomSheetFormaPagamento
import com.example.apkstelladitalia20.databinding.ActivityResumoPedidoBinding
import com.example.apkstelladitalia20.helper.setupToolbar
import com.example.apkstelladitalia20.model.PromocaoEntity

class ResumoPedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResumoPedidoBinding
    private var formaPagamentoSelecionada: String? = null
    private var trocoPara: String? = null
    private var listaCarrinho: ArrayList<PromocaoEntity>? = null // Mantém o carrinho selecionado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResumoPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.includeToolbar)
        setupListeners()
        carregarResumoValores() // 🔥 Mantendo sua função existente!
        carregarResumoDoCarrinho() // 🔥 Nova função para valores reais!
    }

    private fun setupListeners() {
        binding.btnConfirmarPedido.setOnClickListener {
            confirmarPedido()
        }

        binding.txtTrocarPagamento.setOnClickListener {
            mostrarBottomSheetPagamento()
        }
    }

    // 🔥 Sua função original mantida (simula valores fixos se quiser)
    private fun carregarResumoValores() {
        binding.txtSubtotalResumo.text = "R$ 88,90"
        binding.txtTaxaEntregaResumo.text = "Grátis"
        binding.txtTotalResumo.text = "R$ 88,90"
    }

    // 🔥 Nova função: carrega valores reais se listaCarrinho for passada
    private fun carregarResumoDoCarrinho() {
        listaCarrinho = intent.getSerializableExtra("carrinhoSelecionado") as? ArrayList<PromocaoEntity>

        if (!listaCarrinho.isNullOrEmpty()) {
            var subtotal = 0.0
            listaCarrinho?.forEach { item ->
                subtotal += (item.valor ?: 0.0) * (item.quantidade ?: 1)
            }
            val taxaEntrega = 5.0
            val total = subtotal + taxaEntrega

            binding.txtSubtotalResumo.text = "R$ %.2f".format(subtotal)
            binding.txtTaxaEntregaResumo.text = "R$ %.2f".format(taxaEntrega)
            binding.txtTotalResumo.text = "R$ %.2f".format(total)
        } else {
            Toast.makeText(this, "Nenhum item encontrado no carrinho.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarBottomSheetPagamento() {
        val bottomSheet = BottomSheetFormaPagamento { formaPagamento, troco ->
            formaPagamentoSelecionada = formaPagamento
            trocoPara = troco

            val pagamentoTexto = if (formaPagamento == "Dinheiro" && troco != null) {
                "Dinheiro (Troco para R$$troco)"
            } else {
                formaPagamento
            }
            binding.txtFormaPagamento.text = pagamentoTexto
        }
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }

    private fun confirmarPedido() {
        Toast.makeText(this, "Pedido confirmado com sucesso!", Toast.LENGTH_LONG).show()
        finish()
    }
}

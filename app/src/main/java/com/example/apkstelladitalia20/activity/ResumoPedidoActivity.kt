package com.example.apkstelladitalia20.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.bottomsheet.BottomSheetFormaPagamento
import com.example.apkstelladitalia20.databinding.ActivityResumoPedidoBinding

class ResumoPedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResumoPedidoBinding
    private var formaPagamentoSelecionada: String? = null
    private var trocoPara: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResumoPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        carregarResumoValores()
    }

    private fun setupListeners() {
        binding.btnConfirmarPedido.setOnClickListener {
            confirmarPedido()
        }

        binding.includeToolbar.btnVoltar.setOnClickListener {
            finish()
        }

        binding.txtTrocarPagamento.setOnClickListener {
            mostrarBottomSheetPagamento()
        }

    }

    private fun carregarResumoValores() {
        // Aqui simulamos os valores de resumo
        binding.txtSubtotalResumo.text = "R$ 88,90"
        binding.txtTaxaEntregaResumo.text = "Grátis"
        binding.txtTotalResumo.text = "R$ 88,90"
    }

    private fun mostrarBottomSheetPagamento() {
        val bottomSheet = BottomSheetFormaPagamento { formaPagamento, troco ->
            formaPagamentoSelecionada = formaPagamento
            trocoPara = troco

            // Atualizar visualmente na tela
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

        // Depois aqui enviamos o pedido para o Firebase e voltamos para a HomeFragment.
        // Por enquanto, apenas finaliza a activity:
        finish()
    }

    private fun setupToolbar() {
        binding.includeToolbar.btnVoltar.setOnClickListener { finish() }
    }

}

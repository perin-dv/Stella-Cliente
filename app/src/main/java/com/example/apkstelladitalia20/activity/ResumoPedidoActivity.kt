package com.example.apkstelladitalia20.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.databinding.ActivityResumoPedidoBinding

class ResumoPedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResumoPedidoBinding

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

        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun carregarResumoValores() {
        // Aqui simulamos os valores de resumo
        binding.txtSubtotalResumo.text = "R$ 88,90"
        binding.txtTaxaEntregaResumo.text = "Grátis"
        binding.txtTotalResumo.text = "R$ 88,90"
    }

    private fun confirmarPedido() {
        Toast.makeText(this, "Pedido confirmado com sucesso!", Toast.LENGTH_LONG).show()

        // Depois aqui enviamos o pedido para o Firebase e voltamos para a HomeFragment.
        // Por enquanto, apenas finaliza a activity:
        finish()
    }
}

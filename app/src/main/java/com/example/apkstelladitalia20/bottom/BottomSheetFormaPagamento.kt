package com.example.apkstelladitalia20.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.databinding.BottomsheetFormaPagamentoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetFormaPagamento(
    private val onPagamentoSelecionado: (forma: String, troco: String?, tipoPagamento: String) -> Unit,
    private val pagamentoCallback: (forma: String, troco: String?) -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetFormaPagamentoBinding? = null
    private val binding get() = _binding!!

    private var pagamentoEscolhido: String = ""
    private var formaPagamento: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetFormaPagamentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.btnDinheiro.visibility = View.GONE
        binding.txtBanco.text = "Banco"

        setupEscolhaTipoPagamento()
        setupOpcoesPagamento()
    }

    private fun setupEscolhaTipoPagamento() {
        binding.btnPagarApp.setOnClickListener {
            pagamentoEscolhido = "App"
            formaPagamento = ""

            binding.btnPagarApp.setBackgroundResource(R.drawable.bg_button_red)
            binding.btnPagarEntrega.setBackgroundResource(R.drawable.bg_button_gray)

            binding.btnPix.visibility = View.VISIBLE
            binding.btnPix.isEnabled = true
            binding.btnPix.alpha = 1.0f

            binding.btnDinheiro.visibility = View.GONE
            binding.txtBanco.text = "Banco"
        }

        binding.btnPagarEntrega.setOnClickListener {
            pagamentoEscolhido = "Entrega"
            formaPagamento = ""

            binding.btnPagarEntrega.setBackgroundResource(R.drawable.bg_button_red)
            binding.btnPagarApp.setBackgroundResource(R.drawable.bg_button_gray)

            binding.btnPix.visibility = View.GONE
            binding.btnPix.isEnabled = false
            binding.btnPix.alpha = 0.4f

            binding.btnDinheiro.visibility = View.VISIBLE
            binding.txtBanco.text = "Retirada"
        }
    }


    private fun setupOpcoesPagamento() {
        binding.btnCartaoCredito.setOnClickListener {
            formaPagamento = "Cartão de Crédito"
            confirmarSelecao()
        }

        binding.btnCartaoDebito.setOnClickListener {
            formaPagamento = "Cartão de Débito"
            confirmarSelecao()
        }

        binding.btnPix.setOnClickListener {
            formaPagamento = "Pix"
            confirmarSelecao()
        }

        binding.btnDinheiro.setOnClickListener {
            formaPagamento = "Dinheiro"
            binding.btnDinheiro.setBackgroundResource(R.drawable.bg_button_red)
            binding.txtDinheiro.setTextColor(Color.WHITE)

            onPagamentoSelecionado("Dinheiro", null,pagamentoEscolhido)
            pagamentoCallback("Dinheiro", null)
            dismiss()
        }
    }

    private fun confirmarSelecao() {
        onPagamentoSelecionado(formaPagamento, null,pagamentoEscolhido)
        pagamentoCallback(formaPagamento, null)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

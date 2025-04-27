package com.example.apkstelladitalia20.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.apkstelladitalia20.databinding.BottomsheetFormaPagamentoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetFormaPagamento(
    private val onPagamentoSelecionado: (String, String?) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetFormaPagamentoBinding? = null
    private val binding get() = _binding!!

    private var pagamentoEscolhido: String = "App"
    private var formaPagamento: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetFormaPagamentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEscolhaTipoPagamento()
        setupOpcoesPagamento()
        ajustarVisibilidadeDinheiro()
    }

    private fun setupEscolhaTipoPagamento() {
        binding.btnPagarApp.setOnClickListener {
            pagamentoEscolhido = "App"
            binding.layoutDinheiro.visibility = View.GONE
        }
        binding.btnPagarEntrega.setOnClickListener {
            pagamentoEscolhido = "Entrega"
            binding.layoutDinheiro.visibility = View.VISIBLE
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
            confirmarSelecao()
        }
    }

    private fun confirmarSelecao() {
        if (pagamentoEscolhido == "Entrega" && formaPagamento == "Dinheiro") {
            val troco = binding.editTroco.text.toString()
            if (troco.isEmpty()) {
                Toast.makeText(requireContext(), "Informe o valor do troco", Toast.LENGTH_SHORT).show()
                return
            } else {
                onPagamentoSelecionado("Dinheiro", troco)
                dismiss()
            }
        } else {
            onPagamentoSelecionado(formaPagamento, null)
            dismiss()
        }
    }

    private fun ajustarVisibilidadeDinheiro() {
        if (pagamentoEscolhido == "Entrega") {
            binding.layoutDinheiro.visibility = View.VISIBLE
        } else {
            binding.layoutDinheiro.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

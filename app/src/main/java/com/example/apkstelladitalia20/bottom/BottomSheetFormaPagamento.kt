package com.example.apkstelladitalia20.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.databinding.BottomsheetFormaPagamentoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetFormaPagamento(
    private val onPagamentoSelecionado: (String, String?) -> Unit,
    private val pagamentoCallback: (forma: String, troco: String?) -> Unit
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

            binding.btnPagarApp.setBackgroundResource(R.drawable.bg_button_red)
            binding.btnPagarApp.setTextColor(Color.WHITE)

            binding.btnPagarEntrega.setBackgroundResource(R.drawable.bg_button_gray)
            binding.btnPagarEntrega.setTextColor(Color.BLACK)

            binding.btnPix.visibility = View.VISIBLE
            binding.layoutDinheiro.visibility = View.GONE

            binding.btnPix.isEnabled = true
            binding.btnPix.alpha = 1.0f
        }

        binding.btnPagarEntrega.setOnClickListener {
            pagamentoEscolhido = "Entrega"

            binding.btnPagarEntrega.setBackgroundResource(R.drawable.bg_button_red)
            binding.btnPagarEntrega.setTextColor(Color.WHITE)

            binding.btnPagarApp.setBackgroundResource(R.drawable.bg_button_gray)
            binding.btnPagarApp.setTextColor(Color.BLACK)

            binding.btnPix.visibility = View.GONE
            binding.layoutDinheiro.visibility = View.VISIBLE

            binding.btnPix.isEnabled = false
            binding.btnPix.alpha = 0.4f
        }
        binding.editTroco.setOnEditorActionListener { v, actionId, event ->
            val trocoDigitado = binding.editTroco.text.toString().trim()
            if (trocoDigitado.isNotEmpty()) {
                val callback = pagamentoCallback
                callback?.invoke("Dinheiro", trocoDigitado)
                dismiss()
                true
            } else {
                false
            }
        }
    }

    private var dinheiroSelecionado = false

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

            if (!dinheiroSelecionado) {
                // Primeira vez: mostra o campo troco
                binding.layoutDinheiro.visibility = View.VISIBLE
                binding.editTroco.requestFocus()

                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(binding.editTroco, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)

                dinheiroSelecionado = true
            } else {
                // Segunda vez: não precisa de troco
                onPagamentoSelecionado("Dinheiro", null)
                dismiss()
            }
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

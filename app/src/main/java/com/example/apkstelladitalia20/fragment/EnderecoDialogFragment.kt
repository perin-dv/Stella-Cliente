package com.example.apkstelladitalia20.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.databinding.DialogEnderecoBinding

class EnderecoDialogFragment(val appContext: Context,private val onEnderecoSelecionado: (String) -> Unit) : DialogFragment() {
    private lateinit var binding: DialogEnderecoBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogEnderecoBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireContext(), R.style.DialogTheme)
        builder.setView(binding.root)

        binding.btnUsarGps.setOnClickListener {
            // Aqui você pode colocar sua lógica de GPS
            onEnderecoSelecionado("Endereço via GPS (mock)")
            dismiss()
        }

        binding.btnConfirmarEndereco.setOnClickListener {
            val enderecoDigitado = binding.edtEnderecoManual.text.toString()
            if (enderecoDigitado.isNotBlank()) {
                onEnderecoSelecionado(enderecoDigitado)
                dismiss()
            } else {
                binding.edtEnderecoManual.error = "Digite um endereço válido"
            }
        }

        return builder.create()
    }
}

package com.example.apkstelladitalia20.ui.dialog

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.databinding.DialogConfirmacaoPedidoBinding

class ConfirmacaoPedidoDialogFragment(
    private val pedido: PedidoEntity,
    private val onConfirmar: (PedidoEntity) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogConfirmacaoPedidoBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(requireContext())
            .setView(binding.root)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(0x00000000)) // fundo transparente
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)

        binding.txtTitulo.text = "📦 Confirmar Pedido"
        binding.txtDescricao.text = "Deseja realmente confirmar o pedido e prosseguir com o pagamento?"

        binding.btnCancelar.setOnClickListener {
            dismiss()
        }

        binding.btnConfirmar.setOnClickListener {
            onConfirmar(pedido)
            dismiss()
            Toast.makeText(context, "Pedido confirmado! Aguarde...", Toast.LENGTH_SHORT).show()
        }

        return dialog
    }
}

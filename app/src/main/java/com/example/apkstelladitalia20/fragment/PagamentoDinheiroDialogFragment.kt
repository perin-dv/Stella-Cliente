package com.example.apkstelladitalia20.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.R

class PagamentoDinheiroDialogFragment(
    private val pedido: PedidoEntity,
    private val onConfirmar: (PedidoEntity) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_pagamento_dinheiro, null)

        val edtTroco = view.findViewById<EditText>(R.id.edtTroco)
        val btnConfirmar = view.findViewById<Button>(R.id.btnConfirmarPedidoDinheiro)

        builder.setView(view)
        val dialog = builder.create()

        btnConfirmar.setOnClickListener {
            val valorTexto = edtTroco.text.toString()
                .replace("R$", "")
                .replace(",", ".")
                .replace(" ", "")
                .trim()

            val valorTroco = valorTexto.toDoubleOrNull()

            if (valorTroco == null || valorTroco <= 0) {
                Toast.makeText(requireContext(), "Informe um valor válido para troco", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val obsOriginal = pedido.observacao?.trim().orEmpty()
            val obsComTroco = if (obsOriginal.isEmpty()) {
                "Troco para: R$ %.2f".format(valorTroco)
            } else {
                "$obsOriginal\nTroco para: R$ %.2f".format(valorTroco)
            }

            val pedidoFinal = pedido.copy(
                observacao = obsComTroco,
                formaPagamento = "Dinheiro (entrega) - Troco R$ %.2f".format(valorTroco)
            )

            onConfirmar(pedidoFinal)
            dialog.dismiss()
        }

        return dialog
    }
}

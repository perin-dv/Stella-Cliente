package com.example.apkstelladitalia20.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
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
            val valorTroco = edtTroco.text.toString().trim()

            val obsOriginal = pedido.observacao ?: ""
            val obsComTroco = if (valorTroco.isNotEmpty()) {
                "$obsOriginal\nTroco para: R$ $valorTroco"
            } else {
                obsOriginal
            }

            val pedidoFinal = pedido.copy(observacao = obsComTroco)
            onConfirmar(pedidoFinal)
            dialog.dismiss()
        }

        return dialog
    }
}

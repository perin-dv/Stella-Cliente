package com.example.apkstelladitalia20.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.RatingBar
import androidx.fragment.app.DialogFragment
import com.example.apkstelladitalia20.R

class AvaliarPedidoDialogFragment(
    private val onAvaliado: (Int, String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_avaliar_pedido, null)

        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val edtComentario = view.findViewById<EditText>(R.id.edtComentario)

        return AlertDialog.Builder(requireContext())
            .setTitle("Avaliar pedido")
            .setView(view)
            .setPositiveButton("Enviar") { _, _ ->
                val estrelas = ratingBar.rating.toInt()
                val comentario = edtComentario.text.toString()
                onAvaliado(estrelas, comentario)
            }
            .setNegativeButton("Cancelar", null)
            .create()
    }
}

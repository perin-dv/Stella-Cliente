package com.example.apkstelladitalia20.bottom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.apkstelladitalia20.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CartaoBottomSheet(
    private val ultimosDigitos: String,
    private val bandeira: String
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottomsheet_detalhes_cartao, container, false)

        view.findViewById<TextView>(R.id.tvBandeira).text = bandeira.replaceFirstChar { it.uppercase() }
        view.findViewById<TextView>(R.id.tvFinal).text = "Final ••• $ultimosDigitos"
        view.findViewById<Button>(R.id.btnFechar).setOnClickListener {
            dismiss()
        }

        return view
    }
}



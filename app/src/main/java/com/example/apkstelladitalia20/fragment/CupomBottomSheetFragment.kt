package com.example.apkstelladitalia20.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.data.Cupom
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CupomBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_CUPOM = "cupom"

        fun newInstance(cupom: Cupom): CupomBottomSheetFragment {
            val frag = CupomBottomSheetFragment()
            val bundle = Bundle()
            bundle.putString("titulo", cupom.titulo)
            bundle.putString("info", cupom.info)
            bundle.putString("tempo", cupom.tempoRestante)
            frag.arguments = bundle
            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.bottomsheet_cupom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val titulo = view.findViewById<TextView>(R.id.txtCupomTitulo)
        val info = view.findViewById<TextView>(R.id.txtCupomInfo)
        val tempo = view.findViewById<TextView>(R.id.txtCupomTempo)
        val botao = view.findViewById<Button>(R.id.btnUsarAgora)

        titulo.text = arguments?.getString("titulo")
        info.text = arguments?.getString("info")
        tempo.text = arguments?.getString("tempo")

        botao.setOnClickListener {
            Toast.makeText(requireContext(), "Cupom aplicado!", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }
}

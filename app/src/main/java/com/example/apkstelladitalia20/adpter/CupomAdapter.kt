package com.example.apkstelladitalia20.adpter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.data.Cupom

class CupomAdapter(
    private val lista: List<Cupom>,
    private val onCupomClick: (Cupom) -> Unit
) : RecyclerView.Adapter<CupomAdapter.CupomViewHolder>() {

    inner class CupomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo = itemView.findViewById<TextView>(R.id.txtTituloCupom)
        val info = itemView.findViewById<TextView>(R.id.txtInfoCupom)
        val tempo = itemView.findViewById<TextView>(R.id.txtTempoRestante)

        fun bind(cupom: Cupom) {
            titulo.text = cupom.titulo
            info.text = cupom.info
            tempo.text = cupom.tempoRestante

            itemView.setOnClickListener {
                onCupomClick(cupom)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CupomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cupom, parent, false)
        return CupomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CupomViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount() = lista.size
}

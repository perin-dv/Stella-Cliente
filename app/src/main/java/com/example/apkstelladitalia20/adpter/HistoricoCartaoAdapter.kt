package com.example.apkstelladitalia20.adpter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.Entity.TransacaoEntity
import com.example.apkstelladitalia20.R

class HistoricoCartaoAdapter(
    private val lista: List<TransacaoEntity>
) : RecyclerView.Adapter<HistoricoCartaoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvValor: TextView = itemView.findViewById(R.id.tvValor)
        val tvCartao: TextView = itemView.findViewById(R.id.tvCartao)
        val tvDataHora: TextView = itemView.findViewById(R.id.tvDataHora)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historico_transacao, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transacao = lista[position]
        holder.tvValor.text = "R$ %.2f".format(transacao.valor)
        holder.tvCartao.text = "Cartão ••• ${transacao.ultimosDigitosCartao}"
        holder.tvDataHora.text = transacao.dataHora
    }

    override fun getItemCount(): Int = lista.size
}


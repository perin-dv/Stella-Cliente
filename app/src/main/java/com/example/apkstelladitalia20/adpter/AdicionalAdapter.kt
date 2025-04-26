package com.example.apkstelladitalia20.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apkstelladitalia20.databinding.ItemAdicionalBinding
import com.example.apkstelladitalia20.model.Adicional

class AdicionalAdapter(
    private val listaAdicionais: List<Adicional>,
    private val onAdicionalClick: (Adicional) -> Unit
) : RecyclerView.Adapter<AdicionalAdapter.AdicionalViewHolder>() {

    inner class AdicionalViewHolder(val binding: ItemAdicionalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdicionalViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAdicionalBinding.inflate(inflater, parent, false)
        return AdicionalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdicionalViewHolder, position: Int) {
        val adicional = listaAdicionais[position]
        holder.binding.txtNomeAdicional.text = adicional.nome
        holder.binding.txtPrecoAdicional.text = "R$ ${String.format("%.2f", adicional.preco)}"


        holder.itemView.setOnClickListener {
            onAdicionalClick(adicional)
        }
    }

    override fun getItemCount() = listaAdicionais.size
}

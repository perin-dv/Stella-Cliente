package com.example.apkstelladitalia20.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.databinding.ItemBebidaBinding
import com.example.apkstelladitalia20.model.BebidaEntity

class BebidaAdapter(
    private val lista: List<BebidaEntity>,
    private val onAdicionar: (BebidaEntity) -> Unit
) : RecyclerView.Adapter<BebidaAdapter.BebidaViewHolder>() {

    inner class BebidaViewHolder(val binding: ItemBebidaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bebida: BebidaEntity) {
            binding.txtNomeBebida.text = bebida.nome
            binding.txtPrecoBebida.text = "R$ %.2f".format(bebida.preco)

            binding.btnAdicionar.setOnClickListener {
                onAdicionar(bebida)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BebidaViewHolder {
        val binding = ItemBebidaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BebidaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BebidaViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount() = lista.size
}

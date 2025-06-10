package com.example.apkstelladitalia20.adpter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.data.OpcaoPerfil
import com.example.apkstelladitalia20.databinding.ItemOpcaoPerfilBinding

class OpcaoPerfilAdapter(private val opcoes: List<OpcaoPerfil>) :
    RecyclerView.Adapter<OpcaoPerfilAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemOpcaoPerfilBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(opcao: OpcaoPerfil) {
            binding.txtTitulo.text = opcao.titulo
            binding.imgIcone.setImageResource(opcao.icone)
            binding.root.setOnClickListener { opcao.acao() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOpcaoPerfilBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(opcoes[position])
    }

    override fun getItemCount() = opcoes.size
}

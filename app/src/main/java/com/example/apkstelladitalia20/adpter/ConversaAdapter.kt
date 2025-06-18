package com.example.apkstelladitalia20.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.Entity.ConversaEntity
import com.example.apkstelladitalia20.databinding.ItemConversaBinding
import java.text.SimpleDateFormat
import java.util.*

class ConversaAdapter(
    private var lista: List<ConversaEntity>,
    private val onClick: (ConversaEntity) -> Unit
) : RecyclerView.Adapter<ConversaAdapter.ConversaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversaViewHolder {
        val itemBinding = ItemConversaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConversaViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ConversaViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount(): Int = lista.size

    inner class ConversaViewHolder(private val binding: ItemConversaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversa: ConversaEntity) {
            binding.tvTitulo.text = conversa.titulo
            binding.tvMensagem.text = conversa.ultimaMensagem
            binding.txtHora.text = formatarHora(conversa.timestamp)
            binding.root.setOnClickListener { onClick(conversa) }
        }

        private fun formatarHora(timestamp: Long): String {
            return try {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                sdf.format(Date(timestamp))
            } catch (e: Exception) { "--:--" }
        }
    }

    fun atualizar(novaLista: List<ConversaEntity>) {
        lista = novaLista
        notifyDataSetChanged()
    }
}

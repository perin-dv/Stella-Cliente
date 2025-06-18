package com.example.apkstelladitalia20.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.databinding.ItemMensagemClienteBinding
import com.example.apkstelladitalia20.databinding.ItemMensagemLojaBinding
import com.example.apkstelladitalia20.model.Mensagem


class MensagemAdapter(
    private val context: Context,
    private val mensagens: List<Mensagem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TIPO_CLIENTE = 0
    private val TIPO_EMPRESA = 1

    override fun getItemViewType(position: Int): Int {
        return if (mensagens[position].remetente == "cliente") {
            TIPO_CLIENTE
        } else {
            TIPO_EMPRESA
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TIPO_CLIENTE) {
            val itemBinding = ItemMensagemClienteBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
            MensagemClienteViewHolder(itemBinding)
        } else {
            val itemBinding = ItemMensagemLojaBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
            MensagemLojaViewHolder(itemBinding)
        }
    }

    override fun getItemCount(): Int = mensagens.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mensagem = mensagens[position]
        if (holder is MensagemClienteViewHolder) {
            holder.bind(mensagem)
        } else if (holder is MensagemLojaViewHolder) {
            holder.bind(mensagem)

        }
    }

    inner class MensagemClienteViewHolder(
        private val binding: ItemMensagemClienteBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mensagem: Mensagem) {
            binding.textMensagem.text = mensagem.texto

        }
    }

    inner class MensagemLojaViewHolder(
        private val binding: ItemMensagemLojaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mensagem: Mensagem) {
            binding.textMensagem.text = mensagem.texto

        }
    }
}

package com.example.apkstelladitalia20.adpter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.databinding.ItemPedidoResumoBinding
import androidx.recyclerview.widget.ListAdapter



class HistoricoAdapter(
    private val onClick: (PedidoEntity) -> Unit
) : ListAdapter<PedidoEntity, HistoricoAdapter.HistoricoViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPedidoResumoBinding.inflate(inflater, parent, false)
        return HistoricoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoricoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoricoViewHolder(
        private val binding: ItemPedidoResumoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pedido: PedidoEntity) {
            binding.tvLoja.text = pedido.nomeLoja
            binding.tvData.text = "Pedido nº ${pedido.numero} • ${pedido.dataHora}"
            binding.tvStatus.text = "Pedido concluído"
            binding.tvItensResumo.text = pedido.itens.joinToString(" + ") { "${it.quantidade}x ${it.nome}" }

            binding.root.setOnClickListener {
                onClick(pedido)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PedidoEntity>() {
            override fun areItemsTheSame(oldItem: PedidoEntity, newItem: PedidoEntity): Boolean {
                return oldItem.numero == newItem.numero
            }

            override fun areContentsTheSame(oldItem: PedidoEntity, newItem: PedidoEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PedidoEntity>() {
        override fun areItemsTheSame(oldItem: PedidoEntity, newItem: PedidoEntity): Boolean {
            return oldItem.itens == newItem.itens //
        }

        override fun areContentsTheSame(oldItem: PedidoEntity, newItem: PedidoEntity): Boolean {
            return oldItem == newItem
        }
    }

}


package com.example.apkstelladitalia20.adpter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.databinding.ItemPedidoStatusBinding
import com.example.apkstelladitalia20.databinding.ItemResumoVisualizacaoBinding
import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity

class ItensPedidoAdapter(
    private val itens: List<ProdutoCarrinhoEntity>
) : RecyclerView.Adapter<ItensPedidoAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemPedidoStatusBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ItemViewHolder(binding)
    }

    override fun getItemCount(): Int = itens.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(itens[position])
    }

    inner class ItemViewHolder(private val binding: ItemPedidoStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProdutoCarrinhoEntity) {
            binding.tvStatusLoja.text = item.nome // Nome do produto como título
            binding.tvStatusNumeroData.text = item.descricao ?: ""
            binding.tvStatusPedido.text = item.categoria ?: "-"
            binding.tvResumoItensStatus.text = "${item.quantidade}x ${item.nome}"
            binding.tvTotalPedidoStatus.text = "R$ %.2f".format(item.valor)
        }
    }
}

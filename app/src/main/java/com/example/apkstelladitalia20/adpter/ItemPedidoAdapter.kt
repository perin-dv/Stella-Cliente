package com.example.apkstelladitalia20.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.databinding.ItemResumoPedidoBinding

class ItemPedidoAdapter(
    private val itens: List<ProdutoEntity>
) : RecyclerView.Adapter<ItemPedidoAdapter.ItemPedidoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemPedidoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemResumoPedidoBinding.inflate(inflater, parent, false)
        return ItemPedidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemPedidoViewHolder, position: Int) {
        holder.bind(itens[position])
    }

    override fun getItemCount(): Int = itens.size

    inner class ItemPedidoViewHolder(
        private val binding: ItemResumoPedidoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(produto: ProdutoEntity) {
            binding.txtNomeProdutoResumo.text = produto.nome
            binding.txtPrecoProdutoResumo.text = "R$ %.2f".format(produto.getPrecoReal())
            binding.txtQuantidade.text = produto.quantidade.toString()

            // Esconde botões de ação (visualização somente)
            binding.btnAdicionarQuantidade.visibility = View.GONE
            binding.btnRemoverQuantidade.visibility = View.GONE
            binding.btnRemoverItem.visibility = View.GONE

            Glide.with(binding.imgProdutoResumo.context)
                .load(produto.imagem)
                .into(binding.imgProdutoResumo)
        }
    }
}

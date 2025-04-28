package com.example.apkstelladitalia20.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.databinding.ItemResumoPedidoBinding
import com.example.apkstelladitalia20.model.PromocaoEntity

class ResumoPedidoAdapter(
    private val listaResumo: MutableList<PromocaoEntity>,
    private val listener: ResumoListener
) : RecyclerView.Adapter<ResumoPedidoAdapter.MyViewHolder>() {

    interface ResumoListener {
        fun onResumoAtualizado()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemResumoPedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount() = listaResumo.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(listaResumo[position])
    }

    inner class MyViewHolder(private val binding: ItemResumoPedidoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PromocaoEntity) {
            // Nome + Quantidade
            binding.txtNomeProdutoResumo.text = "${item.quantidade ?: 1}x ${item.id ?: "Produto"}"

            // Preço formatado já considerando quantidade
            val precoTotalItem = (item.valor ?: 0.0) * (item.quantidade ?: 1)
            binding.txtPrecoProdutoResumo.text = "R$ %.2f".format(precoTotalItem)

            // --- IMAGEM ---
            // Se quiser setar uma imagem padrão, aqui:
            // binding.imgProdutoResumo.setImageResource(R.drawable.ic_pizza)

            // Atualizar quantidade +/-
            binding.btnAdicionarQuantidade.setOnClickListener {
                item.quantidade = (item.quantidade ?: 1) + 1
                notifyItemChanged(adapterPosition)
                listener.onResumoAtualizado()
            }

            binding.btnRemoverQuantidade.setOnClickListener {
                if ((item.quantidade ?: 1) > 1) {
                    item.quantidade = (item.quantidade ?: 1) - 1
                    notifyItemChanged(adapterPosition)
                    listener.onResumoAtualizado()
                }
            }

            // Remover item do resumo
            binding.btnRemoverItem.setOnClickListener {
                listaResumo.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
                listener.onResumoAtualizado()
            }
        }
    }
}

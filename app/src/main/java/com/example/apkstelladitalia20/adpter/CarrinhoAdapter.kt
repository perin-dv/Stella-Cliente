package com.example.apkstelladitalia20.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.databinding.ItemResumoPedidoBinding
import com.example.apkstelladitalia20.model.PromocaoEntity

class CarrinhoAdapter(
    private val lista: MutableList<ProdutoEntity>,
    private val onResumoAtualizado: () -> Unit,
    private val onRemoverItemBanco: (nome: String) -> Unit
) : RecyclerView.Adapter<CarrinhoAdapter.CarrinhoViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarrinhoViewHolder {
        val binding = ItemResumoPedidoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CarrinhoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarrinhoViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount(): Int = lista.size

    inner class CarrinhoViewHolder(private val binding: ItemResumoPedidoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProdutoEntity) {
            val pos = adapterPosition

            binding.txtNomeProdutoResumo.text = item.nome
            binding.txtPrecoProdutoResumo.text =
                "R$ %.2f".format((item.valor ?: 0.0) * (item.quantidade ?: 1))
            binding.txtQuantidade.text = item.quantidade?.toString() ?: "1"

            try {
                val base64 = item.imagem ?: ""
                val imageBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                val bitmap =
                    android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                Glide.with(binding.imgProdutoResumo.context)
                    .load(bitmap)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(binding.imgProdutoResumo)
            } catch (e: Exception) {
                e.printStackTrace()
                Glide.with(binding.imgProdutoResumo.context)
                    .load(R.drawable.ic_placeholder)
                    .into(binding.imgProdutoResumo)
            }


            binding.btnAdicionarQuantidade.setOnClickListener {
                item.quantidade = (item.quantidade ?: 1) + 1
                notifyItemChanged(pos)
                onResumoAtualizado()
            }

            binding.btnRemoverQuantidade.setOnClickListener {
                val novaQtd = (item.quantidade ?: 1) - 1
                if (novaQtd > 0) {
                    item.quantidade = novaQtd
                    notifyItemChanged(pos)
                    onResumoAtualizado()
                }
            }

            binding.btnRemoverItem.setOnClickListener {
                val nomeRemover = item.nome ?: return@setOnClickListener
                onRemoverItemBanco(nomeRemover) // 🔥 remove da Room
                lista.removeAt(pos)
                notifyItemRemoved(pos)
                onResumoAtualizado()
            }
        }
    }
}

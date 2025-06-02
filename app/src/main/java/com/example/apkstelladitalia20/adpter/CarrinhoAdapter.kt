package com.example.apkstelladitalia20.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.databinding.ItemResumoPedidoBinding

class CarrinhoAdapter(
    private val lista: MutableList<ProdutoCarrinhoEntity>,
    private val onResumoAtualizado: () -> Unit,
    private val onRemoverItemBanco: (nome: String) -> Unit,
    private val onAtualizarQuantidade: (ProdutoCarrinhoEntity) -> Unit


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

        fun bind(item: ProdutoCarrinhoEntity) {
            val pos = adapterPosition

            binding.txtNomeProdutoResumo.text = item.nome
            binding.txtPrecoProdutoResumo.text =
                "R$ %.2f".format(item.valor * item.quantidade)
            binding.txtQuantidade.text = item.quantidade.toString()

            if (!item.imagemUrl.isNullOrEmpty()) {
                try {
                    val base64 = item.imagemUrl.replace("\\s".toRegex(), "")
                    val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                    binding.imgProdutoResumo.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.imgProdutoResumo.setImageResource(R.drawable.ic_placeholder)
                }
            } else {
                binding.imgProdutoResumo.setImageResource(R.drawable.ic_placeholder)
            }


            binding.btnAdicionarQuantidade.setOnClickListener {
                item.quantidade += 1
                notifyItemChanged(pos)
                onAtualizarQuantidade(item)
                onResumoAtualizado()
            }

            binding.btnRemoverQuantidade.setOnClickListener {
                val novaQtd = item.quantidade - 1
                if (novaQtd > 0) {
                    item.quantidade = novaQtd
                    notifyItemChanged(pos)
                    onAtualizarQuantidade(item)
                    onResumoAtualizado()
                }
            }

            binding.btnRemoverItem.setOnClickListener {
                val nomeRemover = item.nome
                onRemoverItemBanco(nomeRemover)
                lista.removeAt(pos)
                notifyItemRemoved(pos)
                onResumoAtualizado()
            }
        }
    }

    fun atualizarLista(novaLista: List<ProdutoCarrinhoEntity>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }
}

package com.stelladitalia.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator

import androidx.recyclerview.widget.RecyclerView

import com.example.apkstelladitalia20.databinding.ItemProdutoBinding

import com.example.apkstelladitalia20.Entity.ProdutoEntity

class ProdutoAdapter(
    private val context: Context,
    private var produtos: List<ProdutoEntity>,
    private val onClick: (ProdutoEntity) -> Unit

) : RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder>() {



    inner class ProdutoViewHolder(val binding: ItemProdutoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(produto: ProdutoEntity) {
            binding.tvNomeProduto.text = produto.nome
            binding.tvPrecoProduto.text = "R$ %.2f".format(produto.getPrecoReal())
            binding.tvDescricaoProduto.text = produto.descricao

            // Exibir imagem do produto
            if (!produto.imagem.isNullOrBlank()) {
                try {
                    val base64Clean = produto.imagem.replace("\\s".toRegex(), "")
                    val imagemBytes = Base64.decode(base64Clean, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imagemBytes, 0, imagemBytes.size)
                    binding.imgProduto.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


            binding.root.setOnClickListener {
                onClick(produto)
            }

            // Animação de destaque
            binding.cardProduto.scaleX = 0.9f
            binding.cardProduto.scaleY = 0.9f
            binding.cardProduto.animate().scaleX(1f).scaleY(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val binding = ItemProdutoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProdutoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        holder.bind(produtos[position])
    }

    override fun getItemCount(): Int = produtos.size

    fun atualizarLista(novaLista: List<ProdutoEntity>) {
        produtos = novaLista
        notifyDataSetChanged()
    }
}


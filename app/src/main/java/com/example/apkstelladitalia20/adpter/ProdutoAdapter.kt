package com.stelladitalia.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.example.apkstelladitalia20.databinding.ItemProdutoBinding

import com.stelladitalia.model.Produto

class ProdutoAdapter(
    private val context: Context,
    private var produtos: List<Produto>,
    private val onClick: (Produto) -> Unit
) : RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder>() {

    inner class ProdutoViewHolder(val binding: ItemProdutoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(produto: Produto) {
            binding.tvNomeProduto.text = produto.nome
            binding.tvPrecoProduto.text = "R$ ${produto.precoOriginal}"


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

    fun atualizarLista(novaLista: List<Produto>) {
        produtos = novaLista
        notifyDataSetChanged()
    }
}


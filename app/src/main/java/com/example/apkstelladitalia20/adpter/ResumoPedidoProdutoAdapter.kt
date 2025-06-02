package com.example.apkstelladitalia20.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.databinding.ItemResumoVisualizacaoBinding
import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity

class ResumoPedidoProdutoVisualAdapter(
    private val listaResumo: MutableList<ProdutoCarrinhoEntity>
) : RecyclerView.Adapter<ResumoPedidoProdutoVisualAdapter.ResumoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResumoViewHolder {
        val binding = ItemResumoVisualizacaoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ResumoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResumoViewHolder, position: Int) {
        holder.bind(listaResumo[position])
    }

    override fun getItemCount(): Int = listaResumo.size

    inner class ResumoViewHolder(private val binding: ItemResumoVisualizacaoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProdutoCarrinhoEntity) {
            binding.txtNomeProdutoResumo.text = item.nome
            binding.txtPrecoProdutoResumo.text = "R$ %.2f".format(item.valor)

            if (!item.imagemUrl.isNullOrEmpty()) {
                try {
                    val imageBytes = Base64.decode(item.imagemUrl, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    Glide.with(binding.imgProdutoResumo.context)
                        .load(bitmap)
                        .placeholder(R.drawable.ic_placeholder)
                        .into(binding.imgProdutoResumo)
                } catch (e: Exception) {
                    binding.imgProdutoResumo.setImageResource(R.drawable.ic_placeholder)
                }
            } else {
                binding.imgProdutoResumo.setImageResource(R.drawable.ic_placeholder)
            }
        }
    }
}

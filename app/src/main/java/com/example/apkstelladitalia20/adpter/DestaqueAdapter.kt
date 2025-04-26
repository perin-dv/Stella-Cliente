package com.example.apkstelladitalia20.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.databinding.ItemDestaqueHorizontalBinding
import com.stelladitalia.model.Produto

class DestaquesAdapter(
    private val context: Context,
    private val destaques: List<Produto>,
    private val onClick: (Produto) -> Unit
) : RecyclerView.Adapter<DestaquesAdapter.DestaqueViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestaqueViewHolder {
        val binding = ItemDestaqueHorizontalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DestaqueViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DestaqueViewHolder, position: Int) {
        holder.bind(destaques[position])
    }

    override fun getItemCount(): Int = destaques.size

    inner class DestaqueViewHolder(private val binding: ItemDestaqueHorizontalBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(produto: Produto) {
            with(binding) {
                txtNome.text = produto.nome
                txtPrecoAtual.text = "R$ ${"%.2f".format(produto.precoAtual)}"
                txtPrecoOriginal.text = "R$ ${"%.2f".format(produto.precoOriginal)}"

                val desconto = calcularDesconto(produto.precoOriginal, produto.precoAtual)
                if (desconto > 0) {
                    seloDesconto.text = "-$desconto%"
                    seloDesconto.visibility = View.VISIBLE
                    txtPrecoOriginal.visibility = View.VISIBLE
                    txtPrecoOriginal.paintFlags = txtPrecoOriginal.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    seloDesconto.visibility = View.GONE
                    txtPrecoOriginal.visibility = View.GONE
                    txtPrecoOriginal.paintFlags = 0
                }

                // "Mais pedido" se for marcado
                seloMaisPedido.visibility = if (produto.maisPedido) View.VISIBLE else View.GONE


                root.setOnClickListener {
                    onClick(produto)
                }

                // Animação suave
                root.alpha = 0f
                root.animate().alpha(1f).setDuration(300).start()
            }
        }

        private fun calcularDesconto(antigo: Double, atual: Double): Int {
            return if (antigo > atual) {
                (((antigo - atual) / antigo) * 100).toInt()
            } else 0
        }
    }
}

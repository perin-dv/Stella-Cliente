package com.example.apkstelladitalia20.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.databinding.ItemAdicionalBinding

class AdicionaisAdapter(
    private val listaAdicionais: List<ProdutoEntity>,
    private val adicionaisSelecionados: MutableList<ProdutoEntity>,
    private val onClick: (ProdutoEntity) -> Unit
) : RecyclerView.Adapter<AdicionaisAdapter.AdicionaisViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdicionaisViewHolder {
        val itemBinding = ItemAdicionalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AdicionaisViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: AdicionaisViewHolder, position: Int) {
        holder.bind(listaAdicionais[position])
    }

    override fun getItemCount(): Int = listaAdicionais.size

    inner class AdicionaisViewHolder(private val binding: ItemAdicionalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(produto: ProdutoEntity) {
            binding.txtNomeAdicional.text = produto.nome
            binding.txtPrecoAdicional.text = "R$ %.2f".format(produto.valor)

            if (!produto.imagemBase64.isNullOrEmpty()) {
                val imagemBytes = Base64.decode(produto.imagemBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imagemBytes, 0, imagemBytes.size)
                binding.imgAdicional.setImageBitmap(bitmap)
            } else {
                binding.imgAdicional.setImageResource(R.drawable.ic_bebidas) // 🔥 caso o produto não tenha imagem, usa padrão
            }

            binding.root.setOnClickListener {
                onClick(produto)
            }

            // Opcional: mudar cor se estiver selecionado
            if (adicionaisSelecionados.contains(produto)) {
                binding.root.setBackgroundResource(R.drawable.bg_categoria_circle) // ou outro fundo que quiser
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_card_gourmet)
            }
        }
    }
}

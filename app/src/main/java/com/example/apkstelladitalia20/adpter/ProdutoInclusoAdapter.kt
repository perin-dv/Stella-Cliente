package com.example.apkstelladitalia20.adpter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.databinding.ItemAdicionalBinding


class ProdutoInclusoAdapter(
    private val produtos: List<ProdutoEntity>
) : RecyclerView.Adapter<ProdutoInclusoAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAdicionalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(produto: ProdutoEntity) {
            binding.txtNomeAdicional.text = produto.nome
            binding.txtPrecoAdicional.text = "R$ %.2f".format(produto.valor)

            produto.imagemBase64?.let { base64 ->
                try {
                    val imagemBytes = Base64.decode(base64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imagemBytes, 0, imagemBytes.size)
                    binding.imgAdicional.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdicionalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(produtos[position])
    }

    override fun getItemCount(): Int = produtos.size
}

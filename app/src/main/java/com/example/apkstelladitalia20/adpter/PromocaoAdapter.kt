// Atualizado PromocaoAdapter.kt - com melhorias
package com.example.apkstelladitalia20.adapter

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.activity.PromocaoDetalhesActivity
import com.example.apkstelladitalia20.databinding.ItemBannerPromocaoBinding
import com.example.apkstelladitalia20.model.PromocaoEntity

class PromocaoAdapter(
    private val context: Context,
    private val lista: MutableList<PromocaoEntity>
) : RecyclerView.Adapter<PromocaoAdapter.PromocaoViewHolder>() {

    inner class PromocaoViewHolder(val binding: ItemBannerPromocaoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(promocao: PromocaoEntity) {
            if (!promocao.imagemBase64.isNullOrEmpty()) {
                try {
                    val base64Clean = promocao.imagemBase64.replace("\\s+".toRegex(), "")
                    val bytes = Base64.decode(base64Clean, Base64.DEFAULT)
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    bmp?.let { binding.imgBanner.setImageBitmap(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            binding.imgBanner.setOnClickListener {
                val intent = Intent(context, PromocaoDetalhesActivity::class.java)
                intent.putExtra("promocaoSelecionada", promocao)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromocaoViewHolder {
        val binding = ItemBannerPromocaoBinding.inflate(LayoutInflater.from(context), parent, false)
        return PromocaoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PromocaoViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount() = lista.size

    fun atualizarLista(novaLista: List<PromocaoEntity>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }
}

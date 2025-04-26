package com.example.apkstelladitalia20.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.databinding.ItemBannerPromocaoBinding
import com.example.apkstelladitalia20.model.Promocao

class PromocaoAdapter(
    private val lista: List<Promocao>,
    private val onClick: (Promocao) -> Unit
) : RecyclerView.Adapter<PromocaoAdapter.PromocaoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromocaoViewHolder {
        val binding = ItemBannerPromocaoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PromocaoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PromocaoViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount(): Int = lista.size

    inner class PromocaoViewHolder(private val binding: ItemBannerPromocaoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(promocao: Promocao) {
            if (!promocao.imagemBase64.isNullOrEmpty()) {
                try {
                    val bytes = Base64.decode(promocao.imagemBase64, Base64.DEFAULT)
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    binding.imgBanner.setImageBitmap(bmp)

                } catch (e: Exception) {
                    e.printStackTrace()
                    // Pode setar uma imagem fallback aqui se quiser
                }
            }

            binding.root.setOnClickListener {
                onClick(promocao)
            }
        }

    }
}

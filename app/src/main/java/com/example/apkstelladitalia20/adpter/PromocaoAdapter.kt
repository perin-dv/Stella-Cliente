package com.example.apkstelladitalia20.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.databinding.ItemBannerPromocaoBinding
import com.example.apkstelladitalia20.model.PromocaoEntity

class PromocaoAdapter(
    private val onClick: (PromocaoEntity) -> Unit
) : ListAdapter<PromocaoEntity, PromocaoAdapter.PromocaoViewHolder>(PromocaoDiffCallback()) {

    inner class PromocaoViewHolder(val binding: ItemBannerPromocaoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(promocao: PromocaoEntity) {
            // Exibe imagem da promoção
            if (!promocao.imagemBase64.isNullOrBlank()) {
                try {
                    val base64Clean = promocao.imagemBase64.replace("\\s+".toRegex(), "")
                    val bytes = Base64.decode(base64Clean, Base64.DEFAULT)
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    bmp?.let {
                        binding.imgBanner.setImageBitmap(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                binding.imgBanner.setImageBitmap(null)
            }

            // Clique correto com referência direta ao objeto
            binding.root.setOnClickListener {
                onClick(promocao)
            }
        }
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromocaoViewHolder {
        val binding = ItemBannerPromocaoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = PromocaoViewHolder(binding)

        return holder
    }

    override fun onBindViewHolder(holder: PromocaoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PromocaoDiffCallback : DiffUtil.ItemCallback<PromocaoEntity>() {
    override fun areItemsTheSame(oldItem: PromocaoEntity, newItem: PromocaoEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PromocaoEntity, newItem: PromocaoEntity): Boolean {
        return oldItem == newItem
    }
}

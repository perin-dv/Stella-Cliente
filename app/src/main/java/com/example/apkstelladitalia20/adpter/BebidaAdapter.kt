package com.example.apkstelladitalia20.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.databinding.ItemBebidaBinding
import com.example.apkstelladitalia20.model.BebidaEntity

class BebidaAdapter(
    private val lista: List<BebidaEntity>,
    private val onAdicionar: (BebidaEntity) -> Unit
) : RecyclerView.Adapter<BebidaAdapter.BebidaViewHolder>() {

    inner class BebidaViewHolder(val binding: ItemBebidaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bebida: BebidaEntity) {
            binding.txtNomeBebida.text = bebida.nome
            binding.txtPrecoBebida.text = "R$ %.2f".format(bebida.preco)




            try {
                val base64Image = bebida.imagem
                val imageBytes =
                    android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT)
                val bitmap =
                    android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                Glide.with(binding.imgBebida.context)
                    .load(bitmap)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(binding.imgBebida)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            binding.btnAdicionar.setOnClickListener {
                onAdicionar(bebida.copy())
            }


        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BebidaViewHolder {
        val binding = ItemBebidaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BebidaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BebidaViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount() = lista.size
}

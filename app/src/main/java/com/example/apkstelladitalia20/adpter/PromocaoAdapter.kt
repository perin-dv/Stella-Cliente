package com.example.apkstelladitalia20.adapter

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.activity.DetalhesPromocaoActivity
import com.example.apkstelladitalia20.databinding.ItemBannerPromocaoBinding
import com.example.apkstelladitalia20.model.PromocaoEntity

class PromocaoAdapter(
    private val lista: List<PromocaoEntity>,
    private val onClick: (PromocaoEntity) -> Unit,
    private val listaPromocoes: MutableList<PromocaoEntity> = mutableListOf<PromocaoEntity>()
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
        fun bind(promocao: PromocaoEntity) {
            if (!promocao.imagemBase64.isNullOrEmpty()) {
                try {
                    val base64Clean = promocao.imagemBase64.replace("\\s".toRegex(), "")
                    val bytes = Base64.decode(base64Clean, Base64.DEFAULT)
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bmp != null) {
                        binding.imgBanner.setImageBitmap(bmp)
                    } else {
                        Log.e("PromocaoAdapter", "Falha ao criar Bitmap: Base64 inválido.")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("PromocaoAdapter", "Erro ao decodificar imagem Base64")
                }
            }
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetalhesPromocaoActivity::class.java)
                intent.putExtra("promocaoSelecionada", promocao)
                itemView.context.startActivity(intent)
            }
        }

    }
    fun atualizarLista(novasPromocoes: List<PromocaoEntity>) {
        this.listaPromocoes.clear()
        this.listaPromocoes.addAll(novasPromocoes)
        notifyDataSetChanged()
    }

}
package com.example.apkstelladitalia20.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.model.SaborEntity
import com.google.firebase.storage.FirebaseStorage

class SaborAdapter(
    private val lista: List<SaborEntity>,
    private val onClick: (SaborEntity) -> Unit
) : RecyclerView.Adapter<SaborAdapter.SaborViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class SaborViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNome: TextView = itemView.findViewById(R.id.txtNomeSabor)
        val txtDescricao: TextView = itemView.findViewById(R.id.txtDescricaoSabor)
        val txtPreco: TextView = itemView.findViewById(R.id.txtPrecoSabor)
        val imgPizza: ImageView = itemView.findViewById(R.id.imgSabor)
        val radioButton: RadioButton = itemView.findViewById(R.id.radioSabor)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaborViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sabor_pizza, parent, false)
        return SaborViewHolder(view)
    }

    override fun onBindViewHolder(holder: SaborViewHolder, position: Int) {
        val item = lista[position]

        holder.txtNome.text = "1/2 ${item.nome}"
        holder.txtDescricao.text = item.descricao
        holder.txtPreco.text = "+ R$ %.2f".format(item.precoAdicional)
        holder.radioButton.isChecked = position == selectedPosition

        val contexto = holder.itemView.context

        if (item.imagem.startsWith("http")) {
            Glide.with(contexto)
                .load(item.imagem)
                .placeholder(R.drawable.ic_pizza_placeholder)
                .into(holder.imgPizza)
        } else {
            // Base64 -> Bitmap
            try {
                val imageBytes = Base64.decode(item.imagem, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.imgPizza.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                holder.imgPizza.setImageResource(R.drawable.ic_pizza_placeholder)
            }
        }


        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition

            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)

            onClick(item)
        }

        holder.radioButton.setOnClickListener {
            holder.itemView.performClick()
        }
    }

    override fun getItemCount(): Int = lista.size
}

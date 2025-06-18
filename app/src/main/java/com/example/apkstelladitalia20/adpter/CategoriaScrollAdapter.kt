package com.example.apkstelladitalia20.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.data.CategoriaScroll

class CategoriaScrollAdapter(
    private val categorias: List<CategoriaScroll>,
    private val onClick: (CategoriaScroll) -> Unit
) : RecyclerView.Adapter<CategoriaScrollAdapter.ViewHolder>() {

    private val lista = mutableListOf<Pair<String, List<ProdutoEntity>>>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icone = itemView.findViewById<ImageView>(R.id.imgIconeCategoria)
        private val texto = itemView.findViewById<TextView>(R.id.txtNomeCategoria)

        fun bind(categoria: CategoriaScroll) {
            icone.setImageResource(categoria.iconeResId)
            texto.text = categoria.nome
            itemView.setOnClickListener {
                onClick(categoria)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria_scroll, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val categoria = categorias[position]
        holder.bind(categoria)

    }
    override fun getItemCount(): Int = categorias.size

    fun getPosicaoCategoria(nome: String): Int {
        return lista.indexOfFirst { it.first == nome }
    }

    fun atualizarLista(novaLista: List<Pair<String, List<ProdutoEntity>>>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }
}

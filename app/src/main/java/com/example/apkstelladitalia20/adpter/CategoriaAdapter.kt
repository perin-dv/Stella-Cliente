package com.example.apkstelladitalia20.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.databinding.ItemCategoriaBinding
import com.stelladitalia.adapters.ProdutoAdapter
import com.example.apkstelladitalia20.Entity.ProdutoEntity

class CategoriaAdapter(
    private val context: Context,
    private val onClickProduto: (ProdutoEntity) -> Unit,

) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {
    private var categorias: MutableList<Pair<String, List<ProdutoEntity>>> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val binding =
            ItemCategoriaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoriaViewHolder(binding)
    }

    fun atualizarLista(novaLista: List<Pair<String, List<ProdutoEntity>>>) {
        categorias.clear()
        categorias.addAll(novaLista)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        holder.bind(categorias[position])
    }

    fun getPosicaoCategoria(nome: String): Int {
        return categorias.indexOfFirst { it.first.equals(nome, ignoreCase = true) }
    }

    override fun getItemCount(): Int = categorias.size

    inner class CategoriaViewHolder(private val binding: ItemCategoriaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(categoria: Pair<String, List<ProdutoEntity>>) {
            binding.tvNomeCategoria.text = categoria.first

            binding.recyclerProdutos.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.recyclerProdutos.adapter =
                ProdutoAdapter(context, categoria.second, onClickProduto)
        }
    }





}

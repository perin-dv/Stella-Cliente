package com.example.apkstelladitalia20.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.databinding.ItemPecaTambemBinding
import com.example.apkstelladitalia20.model.PromocaoEntity

class PecaTambemAdapter(
    private val listaPecaTambem: List<PromocaoEntity>,
    private val onClick: (PromocaoEntity) -> Unit
) : RecyclerView.Adapter<PecaTambemAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemPecaTambemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount() = listaPecaTambem.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(listaPecaTambem[position])
    }

    inner class MyViewHolder(private val binding: ItemPecaTambemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PromocaoEntity) {
            binding.txtNomePecaTambem.text = item.id ?: "Produto"
            binding.txtPrecoPecaTambem.text = "R$ %.2f".format(item.valor ?: 0.0)

            // Se quiser setar uma imagem padrão:
            // binding.imgProdutoPecaTambem.setImageResource(R.drawable.ic_pizza)

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }
}

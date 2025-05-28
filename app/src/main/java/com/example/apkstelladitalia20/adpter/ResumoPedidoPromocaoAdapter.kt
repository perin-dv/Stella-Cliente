package com.example.apkstelladitalia20.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.databinding.ItemResumoPedidoBinding
import com.example.apkstelladitalia20.model.PromocaoEntity

class ResumoPedidoPromocaoAdapter(
    private val listaResumo: ArrayList<PromocaoEntity>,
    private val listener: ResumoListener
) : RecyclerView.Adapter<ResumoPedidoPromocaoAdapter.MyViewHolder>() {

    interface ResumoListener {
        fun onResumoAtualizado()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemResumoPedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount() = listaResumo.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(listaResumo[position])
    }

    inner class MyViewHolder(private val binding: ItemResumoPedidoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PromocaoEntity) {
            val nome = "${item.quantidade ?: 1}x ${item.nome ?: "Promoção"}"
            binding.txtNomeProdutoResumo.text = nome

            val precoTotalItem = (item.valor ?: 0.0) * (item.quantidade ?: 1)
            binding.txtPrecoProdutoResumo.text = "R$ %.2f".format(precoTotalItem)
            binding.txtQuantidade.text = (item.quantidade ?: 1).toString()

            binding.imgProdutoResumo.setImageResource(R.drawable.ic_pizza)


        }
        }
}

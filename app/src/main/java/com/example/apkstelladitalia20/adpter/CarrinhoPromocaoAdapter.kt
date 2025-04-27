package com.example.apkstelladitalia20.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.databinding.ItemCarrinhoPromocaoBinding
import com.example.apkstelladitalia20.model.PromocaoEntity
import com.squareup.picasso.Picasso

class CarrinhoPromocaoAdapter(
    private val listaPromocoes: MutableList<PromocaoEntity>,
    private val listener: CarrinhoListener
) : RecyclerView.Adapter<CarrinhoPromocaoAdapter.CarrinhoViewHolder>() {

    interface CarrinhoListener {
        fun onQuantidadeAlterada()
    }

    inner class CarrinhoViewHolder(val binding: ItemCarrinhoPromocaoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(promocao: PromocaoEntity) {
            binding.txtNomeProduto.text = promocao.titulo
            binding.txtPrecoAtual.text = "R$ %.2f".format(promocao.valor ?: 0.0)
            binding.txtQuantidade.text = promocao.quantidade.toString()

            // Carrega imagem base64
            if (!promocao.imagemBase64.isNullOrEmpty()) {
                Picasso.get()
                    .load(promocao.imagemBase64)
                    .into(binding.imgProdutoCarrinho)
            }

            binding.btnAdicionarQuantidade.setOnClickListener {
                promocao.quantidade++
                notifyItemChanged(adapterPosition)
                listener.onQuantidadeAlterada()
            }

            binding.btnRemoverQuantidade.setOnClickListener {
                if (promocao.quantidade > 1) {
                    promocao.quantidade--
                    notifyItemChanged(adapterPosition)
                    listener.onQuantidadeAlterada()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarrinhoViewHolder {
        val binding = ItemCarrinhoPromocaoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CarrinhoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarrinhoViewHolder, position: Int) {
        holder.bind(listaPromocoes[position])
    }

    override fun getItemCount() = listaPromocoes.size
}

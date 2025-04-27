package com.example.apkstelladitalia20.adapter

import android.graphics.BitmapFactory
import android.util.Base64
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
            binding.txtQuantidade.text = promocao.quantidade.toString()

            // Calcula preços
            val precoUnitario = promocao.valor ?: 0.0
            val precoTotal = precoUnitario * (promocao.quantidade ?: 1)
            val precoOriginal = precoTotal * 1.3 // Pode ser um valor real se quiser depois

            // Atualiza os preços
            binding.txtPrecoAtual.text = "R$ %.2f".format(precoTotal)
            binding.txtPrecoOriginal.text = "R$ %.2f".format(precoOriginal)

            // Carrega imagem base64 ou URL
            if (!promocao.imagemBase64.isNullOrEmpty()) {
                try {
                    if (promocao.imagemBase64.startsWith("http")) {
                        Picasso.get()
                            .load(promocao.imagemBase64)
                            .into(binding.imgProdutoCarrinho)
                    } else {
                        val base64Clean = promocao.imagemBase64.replace("\\s+".toRegex(), "")
                        val bytes = Base64.decode(base64Clean, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        binding.imgProdutoCarrinho.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Botões de adicionar/remover quantidade
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

            // Botão de lixeira - remover item do carrinho
            binding.btnRemoverItem.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listaPromocoes.removeAt(position)
                    notifyItemRemoved(position)
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

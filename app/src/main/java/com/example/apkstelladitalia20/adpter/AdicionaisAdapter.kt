package com.example.apkstelladitalia20.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.databinding.ItemAdicionalBinding
import com.example.apkstelladitalia20.model.CarrinhoViewModel
import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity

class AdicionaisAdapter(
    private val listaAdicionais: List<ProdutoEntity>,
    private val adicionaisSelecionados: MutableList<ProdutoEntity>,
    private val onClick: ((ProdutoEntity) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_TITULO = 0
        private const val VIEW_TYPE_ADICIONAL = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (listaAdicionais[position].id == "titulo") VIEW_TYPE_TITULO else VIEW_TYPE_ADICIONAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_TITULO) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_titulo_adicional, parent, false)
            TituloViewHolder(view)
        } else {
            val itemBinding = ItemAdicionalBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            AdicionalViewHolder(itemBinding)
        }
    }

    override fun getItemCount(): Int = listaAdicionais.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = listaAdicionais[position]
        if (holder is TituloViewHolder) {
            holder.bind(item.descricao ?: "Categoria")
        } else if (holder is AdicionalViewHolder) {
            holder.bind(item)
        }
    }

    inner class TituloViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(titulo: String) {
            itemView.findViewById<TextView>(R.id.txtTituloAdicional).text = titulo
        }
    }

    inner class AdicionalViewHolder(private val binding: ItemAdicionalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(produto: ProdutoEntity) {
            binding.txtNomeAdicional.text = produto.nome ?: "Sem nome"
            binding.txtPrecoAdicional.text = "R$ %.2f".format(produto.getPrecoReal())

            if (!produto.imagem.isNullOrEmpty()) {
                try {
                    val imagemBytes = Base64.decode(produto.imagem, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imagemBytes, 0, imagemBytes.size)
                    binding.imgAdicional.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    binding.imgAdicional.setImageResource(R.drawable.ic_bebidas)
                }
            } else {
                binding.imgAdicional.setImageResource(R.drawable.ic_bebidas)
            }

            binding.root.setOnClickListener {
                val context = binding.root.context
                if (onClick != null) {
                    onClick.invoke(produto)
                } else {
                    val viewModel = ViewModelProvider(context as AppCompatActivity)[CarrinhoViewModel::class.java]
                    viewModel.adicionar(
                        ProdutoCarrinhoEntity(
                            idProduto = produto.id,
                            nome = produto.nome,
                            valor = produto.getPrecoReal(),
                            quantidade = 1,
                            imagemUrl = produto.imagem,
                            descricao = produto.descricao,
                            tipo = "produto"
                        )
                    )
                    Toast.makeText(context, "${produto.nome} adicionado ao carrinho", Toast.LENGTH_SHORT).show()
                }
            }

            if (adicionaisSelecionados.contains(produto)) {
                binding.root.setBackgroundResource(R.drawable.bg_categoria_circle)
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_card_gourmet)
            }
        }
    }
}

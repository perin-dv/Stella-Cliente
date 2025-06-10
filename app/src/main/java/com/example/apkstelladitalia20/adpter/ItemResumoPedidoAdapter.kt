package com.example.apkstelladitalia20.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.databinding.ItemResumoPedidoBinding
import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ItemResumoPedidoAdapter(
    private val itens: List<ProdutoCarrinhoEntity>,
    private val uidEmpresa: String
) : RecyclerView.Adapter<ItemResumoPedidoAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemResumoPedidoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemResumoPedidoBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = itens.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itens[position]
        Log.d("DEBUG_IMAGEM", "Nome: ${item.nome}, idProduto: ${item.idProduto}, imagemUrl: ${item.imagemUrl}")

        holder.binding.txtNomeProdutoResumo.text = item.nome
        holder.binding.txtPrecoProdutoResumo.text = "R$ %.2f".format(item.valor * item.quantidade)
        holder.binding.txtQuantidade.text = item.quantidade.toString()

        val url = item.imagemUrl ?: ""
        val isProvavelBase64 = url.startsWith("data:image") || url.length > 500

        if (!url.isNullOrEmpty() && !isProvavelBase64) {
            // Carrega URL normal
            Picasso.get()
                .load(url)
                .placeholder(R.drawable.ic_pizza)
                .error(R.drawable.ic_pizza)
                .into(holder.binding.imgProdutoResumo)
        } else if (!item.idProduto.startsWith("promo_")) {
            // Busca do Firebase se não for promoção
            val ref = FirebaseDatabase.getInstance()
                .getReference("empresa")
                .child(uidEmpresa)
                .child("produtos")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var urlImagem: String? = null

                    for (produtoSnap in snapshot.children) {
                        val nomeProduto = produtoSnap.child("nome").getValue(String::class.java)
                        if (nomeProduto != null && nomeProduto.contains(item.nome, ignoreCase = true)) {
                            // Primeiro tenta imagemUrl
                            urlImagem = produtoSnap.child("imagemUrl").getValue(String::class.java)

                            if (urlImagem.isNullOrEmpty()) {
                                // Se não tiver imagemUrl, tenta Base64 do campo imagem
                                val base64 = produtoSnap.child("imagem").getValue(String::class.java)
                                if (!base64.isNullOrEmpty()) {
                                    try {
                                        val imageBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                        holder.binding.imgProdutoResumo.setImageBitmap(bitmap)
                                        return
                                    } catch (e: Exception) {
                                        Log.e("ItemResumoPedido", "Erro ao decodificar imagem base64: ${e.message}")
                                    }
                                }
                            } else {
                                Picasso.get()
                                    .load(urlImagem)
                                    .placeholder(R.drawable.ic_pizza)
                                    .error(R.drawable.ic_pizza)
                                    .into(holder.binding.imgProdutoResumo)
                                return
                            }
                        }
                    }

                    // Fallback padrão
                    holder.binding.imgProdutoResumo.setImageResource(R.drawable.ic_pizza)
                }

                override fun onCancelled(error: DatabaseError) {
                    holder.binding.imgProdutoResumo.setImageResource(R.drawable.ic_pizza)
                }
            })
        } else {
            // Se for promoção, tenta buscar imagemBase64 da promoção
            val idPromo = item.idProduto.removePrefix("promo_")
            val refPromo = FirebaseDatabase.getInstance()
                .getReference("empresa")
                .child(uidEmpresa)
                .child("promocoes")
                .child(idPromo)

            refPromo.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val base64 = snapshot.child("imagemBase64").getValue(String::class.java)
                    if (!base64.isNullOrEmpty()) {
                        try {
                            val imageBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            holder.binding.imgProdutoResumo.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            Log.e("ItemResumoPromo", "Erro ao decodificar img promo: ${e.message}")
                            holder.binding.imgProdutoResumo.setImageResource(R.drawable.ic_pizza)
                        }
                    } else {
                        holder.binding.imgProdutoResumo.setImageResource(R.drawable.ic_pizza)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    holder.binding.imgProdutoResumo.setImageResource(R.drawable.ic_pizza)
                }
            })
        }

        // Esconde botões
        holder.binding.btnAdicionarQuantidade.visibility = View.GONE
        holder.binding.btnRemoverQuantidade.visibility = View.GONE
        holder.binding.btnRemoverItem.visibility = View.GONE
    }
}

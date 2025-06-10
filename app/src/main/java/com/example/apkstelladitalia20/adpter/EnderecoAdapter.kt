package com.example.apkstelladitalia20.adapter

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.Entity.EnderecoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.activity.EnderecoBottomSheet

class EnderecoAdapter(
    private val lista: MutableList<EnderecoEntity> = mutableListOf(),
    private val onSelecionar: (EnderecoEntity) -> Unit,
    private val onEditar: (EnderecoEntity) -> Unit,
    private val onDeletar: (EnderecoEntity) -> Unit
) : RecyclerView.Adapter<EnderecoAdapter.ViewHolder>() {

    private var enderecoSelecionado: EnderecoEntity? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardEndereco: CardView = itemView.findViewById(R.id.cardEndereco)
        val txtTitulo: TextView = itemView.findViewById(R.id.txtTituloEndereco)
        val txtEndereco: TextView = itemView.findViewById(R.id.txtEndereco)
        val txtComplemento: TextView = itemView.findViewById(R.id.txtComplemento)
        val icSelecionado: ImageView = itemView.findViewById(R.id.icSelecionado)
        val btnMais: ImageView = itemView.findViewById(R.id.btnMaisEndereco)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_endereco, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val endereco = lista[position]

        holder.txtTitulo.text = endereco.referencia.ifEmpty { "Endereço" }
        holder.txtEndereco.text =
            "${endereco.rua}, ${endereco.numero} - ${endereco.bairro}, ${endereco.cidade} - ${endereco.estado}"
        holder.txtComplemento.text = "CEP: ${endereco.cep}"

        val isSelecionado = enderecoSelecionado == endereco
        holder.icSelecionado.visibility = if (isSelecionado) View.VISIBLE else View.INVISIBLE
        holder.cardEndereco.setCardBackgroundColor(
            if (isSelecionado) holder.itemView.context.getColor(R.color.white)
            else holder.itemView.context.getColor(R.color.white)
        )

        holder.cardEndereco.setOnClickListener {
            enderecoSelecionado = endereco
            notifyDataSetChanged()
            EnderecoBottomSheet(
                endereco = endereco,
                onEditar = { onEditar(endereco) },
                onExcluido = {
                    lista.remove(endereco)
                    notifyDataSetChanged()
                }
            ).show(
                (holder.itemView.context as AppCompatActivity).supportFragmentManager,
                "EnderecoBottomSheet"
            )
        }

        holder.btnMais.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.btnMais)
            popup.menuInflater.inflate(R.menu.menu_endereco, popup.menu)
            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.menu_editar -> {
                        onEditar(endereco)
                        true
                    }

                    R.id.menu_deletar -> {
                        onDeletar(endereco)
                        true
                    }

                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount() = lista.size

    fun atualizarLista(novaLista: List<EnderecoEntity>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }
}

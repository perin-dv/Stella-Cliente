package com.example.apkstelladitalia20.adpter

import android.animation.ObjectAnimator
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.activity.DetalhesPedidoBottomSheet
import com.example.apkstelladitalia20.databinding.ItemPedidoResumoBinding
import com.example.apkstelladitalia20.databinding.ItemPedidoStatusBinding
import com.example.apkstelladitalia20.databinding.ItemTituloDataBinding
import com.example.apkstelladitalia20.util.estaAtrasado

sealed class ItemHistorico
data class TituloData(val titulo: String) : ItemHistorico()
data class PedidoItem(val pedido: PedidoEntity) : ItemHistorico()

class HistoricoAdapter(
    private val onClick: (PedidoEntity) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val itens = mutableListOf<ItemHistorico>()

    fun submitGroupedList(mapa: Map<String, List<PedidoEntity>>) {
        itens.clear()
        for ((data, pedidosDoDia) in mapa) {
            itens.add(TituloData(data))
            itens.addAll(pedidosDoDia.map { PedidoItem(it) })
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = itens[position]) {
            is TituloData -> 0
            is PedidoItem -> {
                val status = item.pedido.status?.trim()?.lowercase() ?: ""
                if (status in listOf("finalizado", "entregue", "concluido", "cancelado")) 2 else 1
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> TituloViewHolder(
                ItemTituloDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            1 -> PedidoStatusViewHolder(
                ItemPedidoStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            2 -> PedidoResumoViewHolder(
                ItemPedidoResumoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> throw IllegalArgumentException("Tipo desconhecido")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = itens[position]) {
            is TituloData -> (holder as TituloViewHolder).bind(item)
            is PedidoItem -> {
                when (holder) {
                    is PedidoStatusViewHolder -> holder.bind(item.pedido)
                    is PedidoResumoViewHolder -> holder.bind(item.pedido)
                }
            }
        }
    }

    override fun getItemCount(): Int = itens.size

    inner class TituloViewHolder(private val binding: ItemTituloDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tituloData: TituloData) {
            binding.tvTituloData.text = tituloData.titulo
        }
    }

    inner class PedidoStatusViewHolder(private val binding: ItemPedidoStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pedido: PedidoEntity) {
            binding.tvStatusLoja.text = "Stella D’Italia - Maringá"
            binding.tvStatusNumeroData.text = "Pedido nº ${pedido.numero} • ${pedido.dataHora}"

            val status = pedido.status?.trim()?.lowercase()

            binding.tvStatusPedido.text = when (status) {
                "aguardando", "aguardando_confirmacao" -> "Aguardando a empresa confirmar seu pedido"
                "confirmado" -> "Pedido confirmado"
                "entrega" -> "Saiu para entrega"
                else -> "Aguardando"
            }

            val progressoFinal = when (status) {
                "aguardando", "aguardando_confirmacao" -> 25
                "confirmado" -> 50
                "entrega" -> 75
                else -> 10
            }

            val progressColor = when (status) {
                "confirmado" -> R.color.green
                else -> R.color.vermelho
            }

            binding.progressoPedido.progressTintList =
                binding.root.context.getColorStateList(progressColor)

            val anim = ObjectAnimator.ofInt(binding.progressoPedido, "progress", 0, progressoFinal)
            anim.duration = 1000
            anim.interpolator = DecelerateInterpolator()
            anim.start()

            binding.tvResumoItensStatus.text = pedido.itens.joinToString(" + ") {
                "${it.quantidade}x ${it.nome}"
            }

            binding.tvTotalPedidoStatus.text = "Total: R$ %.2f".format(pedido.total)

            binding.tvPedidoAtrasado.visibility =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pedido.estaAtrasado())
                    View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                onClick(pedido)
                DetalhesPedidoBottomSheet(pedido).show(
                    (it.context as AppCompatActivity).supportFragmentManager,
                    "detalhes"
                )
            }
        }
    }

    inner class PedidoResumoViewHolder(private val binding: ItemPedidoResumoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pedido: PedidoEntity) {
            binding.tvLoja.text = "Stella D’Italia - Maringá"
            binding.tvData.text = "Pedido nº ${pedido.numero} • ${pedido.dataHora}"
            binding.tvItensResumo.text = pedido.itens.joinToString(" + ") {
                "${it.quantidade}x ${it.nome}"
            }

            when (pedido.status?.trim()?.lowercase()) {
                "entregue", "concluido", "finalizado" -> {
                    binding.tvStatus.text = "Entregue"
                    binding.tvStatus.setTextColor(binding.root.context.getColor(R.color.green))
                    binding.icStatusResumo.setImageResource(R.drawable.ic_check_circle)
                    binding.layoutStatus.visibility = View.VISIBLE
                }

                "cancelado" -> {
                    binding.tvStatus.text = "Cancelado"
                    binding.tvStatus.setTextColor(binding.root.context.getColor(R.color.vermelho))
                    binding.icStatusResumo.setImageResource(R.drawable.ic_cancel)
                    binding.layoutStatus.visibility = View.VISIBLE
                }

                else -> {
                    binding.layoutStatus.visibility = View.GONE
                }
            }

            binding.root.setOnClickListener {
                onClick(pedido)
                DetalhesPedidoBottomSheet(pedido).show(
                    (it.context as AppCompatActivity).supportFragmentManager,
                    "detalhes"
                )
            }
        }
    }
}

package com.example.apkstelladitalia20.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.adapter.ItemResumoPedidoAdapter
import com.example.apkstelladitalia20.databinding.BottomsheetDetalhesPedidoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DetalhesPedidoBottomSheet(private val pedido: PedidoEntity) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomsheetDetalhesPedidoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = BottomsheetDetalhesPedidoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvNomeLoja.text = "Stella D’Italia - Maringá"
        binding.tvNumeroPedido.text = "Pedido nº ${pedido.numero} • ${pedido.dataHora}"

        aplicarStatusVisualGourmet()

        val statusTexto = when (pedido.status) {
            "aguardando" -> "Aguardando confirmação"
            "confirmado" -> "Pedido confirmado"
            "entrega" -> "Saiu para entrega"
            "concluido" -> "Pedido entregue"
            else -> "-"
        }

        val statusIcone = when (pedido.status) {
            "aguardando" -> R.drawable.ic_relogio
            "confirmado" -> R.drawable.ic_check
            "entrega" -> R.drawable.ic_entrega
            "concluido" -> R.drawable.ic_caixa
            else -> R.drawable.ic_relogio
        }

        val statusCor = when (pedido.status) {
            "aguardando" -> android.R.color.holo_red_dark
            "confirmado" -> android.R.color.holo_green_dark
            "entrega" -> android.R.color.holo_blue_dark
            "concluido" -> android.R.color.darker_gray
            else -> android.R.color.holo_red_dark
        }

        binding.tvStatusPedido.text = statusTexto
        binding.tvStatusPedido.setTextColor(ContextCompat.getColor(requireContext(), statusCor))
        binding.icStatusAtual.setImageResource(statusIcone)
        binding.icStatusAtual.setColorFilter(ContextCompat.getColor(requireContext(), statusCor))

        val uidEmpresa = requireContext()
            .getSharedPreferences("appStella", Context.MODE_PRIVATE)
            .getString("uidEmpresa", "") ?: ""

        binding.recyclerItens.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerItens.adapter = ItemResumoPedidoAdapter(pedido.itens, uidEmpresa)

        val subtotal = pedido.itens.sumOf { it.valor * it.quantidade }
        binding.tvResumoSubtotal.text = "Subtotal: R$ %.2f".format(subtotal)
        binding.tvResumoDescontos.text = "Descontos: - R$ %.2f".format(pedido.desconto)
        binding.tvResumoEntrega.text =
            if (pedido.entrega.equals("Grátis", true) || pedido.entrega.equals(
                    "0.0",
                    true
                ) || pedido.entrega.isNullOrBlank()
            ) {
                "Entrega: Grátis"
            } else {
                "Entrega: R$ %.2f".format(pedido.entrega.toDoubleOrNull() ?: 0.0)
            }

        binding.tvResumoTotal.text = "Total: R$ %.2f".format(pedido.total)
        binding.tvMetodoPagamento.text = pedido.formaPagamento ?: "-"

        val enderecoFormatado = pedido.enderecoEntrega?.let {
            it.removePrefix("{").removeSuffix("}").split(", ").associate {
                val (k, v) = it.split("=")
                k to v
            }
        }?.let {
            "${it["rua"]}, ${it["numero"]} - ${it["bairro"]}\n${it["cidade"]} - ${it["estado"]}, CEP: ${it["cep"]}\nReferência: ${it["referencia"]}"
        } ?: "-"

        binding.tvEnderecoEntrega.text = enderecoFormatado
    }

    private fun aplicarStatusVisualGourmet() {
        val status = pedido.status?.lowercase()?.trim() ?: ""
        val progresso: Int
        val cor: Int
        val textoExplicativo: String

        when (status) {
            "aguardando", "aguardando_confirmacao" -> {
                progresso = 25
                cor = R.color.vermelho
                textoExplicativo = "Aguardando a empresa confirmar seu pedido"
            }

            "confirmado" -> {
                progresso = 50
                cor = R.color.green
                textoExplicativo = "Seu pedido foi confirmado pela loja"
            }

            "entrega" -> {
                progresso = 75
                cor = R.color.blue
                textoExplicativo = "O entregador está a caminho"
            }

            "concluido" -> {
                progresso = 100
                cor = R.color.green
                textoExplicativo = "Pedido finalizado com sucesso"
            }

            else -> {
                progresso = 0
                cor = R.color.gray
                textoExplicativo = "-"
            }
        }

        // Anima e aplica corretamente
        binding.progressoStatus.post {
            binding.progressoStatus.progress = 0
            binding.progressoStatus.progressDrawable = null
            binding.progressoStatus.progressDrawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.progress_gourmet_drawable)
            binding.progressoStatus.progressTintList =
                ContextCompat.getColorStateList(requireContext(), cor)
            binding.progressoStatus.progress = progresso

            binding.tvLinhaStatusCinza.text = textoExplicativo
        }
    }
}

package com.example.apkstelladitalia20.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.fragment.app.Fragment
import com.example.apkstelladitalia20.activity.DetalhesPedidoActivity

import com.example.apkstelladitalia20.adpter.HistoricoAdapter
import com.example.apkstelladitalia20.databinding.FragmentPedidosBinding
import com.example.apkstelladitalia20.repository.PedidoRepository

class PedidoFragment : Fragment() {

    private var _binding: FragmentPedidosBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapterHistorico: HistoricoAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPedidosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterHistorico = HistoricoAdapter { pedido ->
            val intent = Intent(requireContext(), DetalhesPedidoActivity::class.java)
            intent.putExtra("pedido", pedido)
            startActivity(intent)
        }

        binding.recyclerHistoricoPedidos.adapter = adapterHistorico

        carregarPedidos()
    }

    private fun carregarPedidos() {
        val pedidosFake = PedidoRepository.getPedidos()

        val ativo = pedidosFake.find { it.status != "concluido" }
        val historico = pedidosFake.filter { it.status == "concluido" }

        if (ativo != null) {
            binding.cardPedidoStatus.visibility = View.VISIBLE

            binding.tvStatusLoja.text = ativo.nomeLoja
            binding.tvStatusNumeroData.text = "Pedido nº ${ativo.numero} • ${ativo.dataHora}"
            binding.tvResumoItensStatus.text = ativo.itens.joinToString(" + ") { "${it.quantidade}x ${it.nome}" }
            binding.tvTotalPedidoStatus.text = "Total: R$ %.2f".format(ativo.total)
            binding.tvStatusPedido.text = when (ativo.status) {
                "confirmado" -> "Pedido confirmado"
                "entrega" -> "Saiu para entrega"
                else -> "Aguardando confirmação"
            }

            binding.cardPedidoStatus.setOnClickListener {
                val intent = Intent(requireContext(), DetalhesPedidoActivity::class.java)
                intent.putExtra("pedido", ativo)
                startActivity(intent)
            }
        }

        if (historico.isNotEmpty()) {
            binding.tvHistoricoTitulo.visibility = View.VISIBLE
            // aqui você pode seguir o mesmo padrão para listar histórico no RecyclerView, se quiser
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

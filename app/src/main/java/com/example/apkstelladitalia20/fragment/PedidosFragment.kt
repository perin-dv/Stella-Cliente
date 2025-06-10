package com.example.apkstelladitalia20.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.activity.DetalhesPedidoActivity
import com.example.apkstelladitalia20.adpter.HistoricoAdapter
import com.example.apkstelladitalia20.databinding.FragmentPedidosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class PedidosFragment : Fragment() {

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
            intent.putExtra("idPedido", pedido.id)
            intent.putExtra("clienteId", FirebaseAuth.getInstance().currentUser?.uid)
            startActivity(intent)

        }
        binding.recyclerHistoricoPedidos.adapter = adapterHistorico
        binding.recyclerHistoricoPedidos.layoutManager = LinearLayoutManager(requireContext())

        binding.recyclerHistoricoPedidos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHistoricoPedidos.adapter = adapterHistorico
        carregarPedidosFirebase()
    }

    private fun carregarPedidosFirebase() {
        val sharedPrefs = requireContext().getSharedPreferences("appStella", Context.MODE_PRIVATE)
        val uidCliente = sharedPrefs.getString("uidCliente", null)


        if (uidCliente.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Erro: Usuário não está logado", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("clientes")
            .child(uidCliente)
            .child("pedidos")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pedidos = mutableListOf<PedidoEntity>()

                snapshot.children.forEach { pedidoSnap ->
                    val pedido = pedidoSnap.getValue(PedidoEntity::class.java)
                    if (pedido != null) {
                        pedidos.add(pedido)
                    }
                }

                Log.d("UID_DEBUG", "uidCliente usado: $uidCliente")
                atualizarUIComPedidos(pedidos)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Erro ao carregar pedidos", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun atualizarUIComPedidos(pedidos: List<PedidoEntity>) {
        if (pedidos.isEmpty()) {
            binding.recyclerHistoricoPedidos.visibility = View.GONE
            binding.recyclerHistoricoPedidos.visibility = View.GONE
            binding.layoutNenhumPedido.visibility = View.VISIBLE
            return
        }

        binding.recyclerHistoricoPedidos.visibility = View.VISIBLE
        binding.recyclerHistoricoPedidos.visibility = View.VISIBLE
        binding.layoutNenhumPedido.visibility = View.GONE

        // Agrupar por data formatada
        val pedidosAgrupados = pedidos
            .sortedByDescending { it.dataHora }
            .groupBy { formatarDataTitulo(it.dataHora) }

        adapterHistorico.submitGroupedList(pedidosAgrupados)
    }

    private fun formatarDataTitulo(dataHora: String?): String {
        if (dataHora == null) return ""

        val formatoEntrada = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val formatoDia = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val formatoSaida = SimpleDateFormat("EEE, dd MMMM", Locale("pt", "BR"))

        return try {
            val data = formatoEntrada.parse(dataHora)
            val hoje = Calendar.getInstance()
            val dataPedido = Calendar.getInstance().apply { time = data!! }

            return when {
                formatoDia.format(data) == formatoDia.format(hoje.time) -> "Hoje"
                formatoDia.format(data) == formatoDia.format(hoje.apply { add(Calendar.DATE, -1) }.time) -> "Ontem"
                else -> formatoSaida.format(data)
            }
        } catch (e: Exception) {
            dataHora
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

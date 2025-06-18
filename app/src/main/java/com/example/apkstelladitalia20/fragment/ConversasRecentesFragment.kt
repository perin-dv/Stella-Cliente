package com.example.apkstelladitalia20.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.adapter.MensagemAdapter
import com.example.apkstelladitalia20.databinding.FragmentConversasRecentesBinding
import com.example.apkstelladitalia20.model.Mensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ConversasRecentesFragment : Fragment() {

    private lateinit var binding: FragmentConversasRecentesBinding
    private lateinit var adapter: MensagemAdapter
    private val listaMensagens = mutableListOf<Mensagem>()
    private var idPedido: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConversasRecentesBinding.inflate(inflater, container, false)
        idPedido = requireActivity().intent.getStringExtra("idPedido")

        adapter = MensagemAdapter(requireContext(), listaMensagens)
        binding.recyclerConversas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerConversas.adapter = adapter

        carregarMensagens()

        return binding.root
    }

    private fun carregarMensagens() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val pedidoId = idPedido
        if (pedidoId.isNullOrEmpty()) {
            // UID válido mas sem pedido ainda → mostra imagem
            binding.layoutVazio.visibility = View.VISIBLE
            return
        }


        val ref = FirebaseDatabase.getInstance()
            .getReference("conversas_clientes/$uid/$pedidoId/mensagens")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaMensagens.clear()
                for (msgSnap in snapshot.children) {
                    val mensagem = msgSnap.getValue(Mensagem::class.java)
                    if (mensagem != null) {
                        listaMensagens.add(mensagem)
                    }
                    Log.d("DEBUG_UID", "UID atual = ${FirebaseAuth.getInstance().currentUser?.uid}")

                }

                if (listaMensagens.isEmpty()) {
                    binding.layoutVazio.visibility = View.VISIBLE
                } else {
                    binding.layoutVazio.visibility = View.GONE
                }
                binding.layoutVazio.visibility = if (listaMensagens.isEmpty()) View.VISIBLE else View.GONE


                adapter.notifyDataSetChanged()
                binding.recyclerConversas.scrollToPosition(listaMensagens.size - 1)


            }

            override fun onCancelled(error: DatabaseError) {
                ref.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        listaMensagens.clear()

                        for (msgSnap in snapshot.children) {
                            val mensagem = msgSnap.getValue(Mensagem::class.java)
                            if (mensagem != null) {
                                listaMensagens.add(mensagem)
                            }
                        }

                        adapter.notifyDataSetChanged()
                        binding.layoutVazio.visibility = if (listaMensagens.isEmpty()) View.VISIBLE else View.GONE
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // ERRO ao buscar no Firebase → mostra layout de vazio mesmo assim
                        binding.layoutVazio.visibility = View.VISIBLE
                        Log.e("Conversas", "Erro ao carregar mensagens: ${error.message}")
                    }
                })

            }
        })
    }


}

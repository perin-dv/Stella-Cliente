package com.example.apkstelladitalia20.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.Entity.ConversaEntity
import com.example.apkstelladitalia20.activity.ChatActivity
import com.example.apkstelladitalia20.activity.ConversasActivity
import com.example.apkstelladitalia20.adapter.ConversaAdapter
import com.example.apkstelladitalia20.databinding.FragmentListaConversasBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ConversasFinalizadasFragment : Fragment() {

    private lateinit var binding: FragmentListaConversasBinding
    private lateinit var adapter: ConversaAdapter
    private val listaConversas = mutableListOf<ConversaEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListaConversasBinding.inflate(inflater, container, false)

        adapter = ConversaAdapter(listaConversas) { conversa ->
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("uidPedido", conversa.idPedido)
            intent.putExtra("uidCliente", conversa.uidCliente)
            context?.startActivity(intent)

        }

        binding.recyclerConversas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerConversas.adapter = adapter

        carregarConversas()
        return binding.root
    }

    private fun carregarConversas() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Log.e("Finalizadas", "UID é nulo")
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("conversas_clientes/$uid")
        Log.d("Finalizadas", "Buscando conversas de: $uid")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaConversas.clear()

                Log.d("Finalizadas", "Total de pedidos encontrados: ${snapshot.childrenCount}")

                snapshot.children.forEach { pedidoSnap ->
                    val id = pedidoSnap.key.orEmpty()
                    val status = pedidoSnap.child("status").getValue(String::class.java)?.trim()?.lowercase() ?: ""

                    Log.d("Finalizadas", "Pedido $id - status: '$status'")

                    if (status == "finalizada") {
                        val mensagensSnap = pedidoSnap.child("mensagens")
                        val ultimaMensagemSnap = mensagensSnap.children.lastOrNull()

                        val ultimaMensagem = ultimaMensagemSnap?.child("texto")?.getValue(String::class.java) ?: "(sem mensagem)"
                        val timestamp = ultimaMensagemSnap?.key?.toLongOrNull() ?: 0L
                        val valor = pedidoSnap.child("valor").getValue(Double::class.java)
                            ?: pedidoSnap.child("valor").getValue(String::class.java)?.toDoubleOrNull()
                            ?: 0.0
                        val data = pedidoSnap.child("data").getValue(String::class.java) ?: ""
                        val titulo = pedidoSnap.child("titulo").getValue(String::class.java) ?: "Pedido $id"

                        val conversa = ConversaEntity(
                            idPedido = id,
                            valor = valor,
                            status = status,
                            data = data,
                            timestamp = timestamp,
                            ultimaMensagem = ultimaMensagem,
                            titulo = titulo,
                            uidCliente = uid
                        )

                        Log.d("Finalizadas", "Adicionando conversa: $titulo com msg: $ultimaMensagem")

                        listaConversas.add(conversa)
                    }
                }

                Log.d("Finalizadas", "Conversas finalizadas carregadas: ${listaConversas.size}")
                adapter.atualizar(listaConversas)
                binding.layoutVazio.visibility = if (listaConversas.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Finalizadas", "Erro Firebase: ${error.message}")
            }
        })
    }
}

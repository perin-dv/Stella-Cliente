package com.example.apkstelladitalia20.activity

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.adapter.MensagemAdapter
import com.example.apkstelladitalia20.databinding.ActivityChatBinding
import com.example.apkstelladitalia20.model.Mensagem
import com.google.firebase.database.*
import android.view.View


class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var mensagensRef: DatabaseReference
    private lateinit var adapter: MensagemAdapter
    private val listaMensagens = mutableListOf<Mensagem>()

    private lateinit var uidPedido: String
    private lateinit var uidCliente: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uidPedido = intent.getStringExtra("uidPedido") ?: ""
        uidCliente = intent.getStringExtra("uidCliente") ?: ""



        setavolvar()


        binding.textTituloPedido.text = "Pedido #$uidPedido"

        if (uidPedido.isBlank() || uidCliente.isBlank()) {
            Toast.makeText(this, "Erro ao carregar conversa", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        mensagensRef = FirebaseDatabase.getInstance()
            .getReference("conversas_clientes")
            .child(uidCliente)
            .child(uidPedido)
            .child("mensagens")

        adapter = MensagemAdapter(this@ChatActivity, listaMensagens)
        binding.recyclerConversas.layoutManager = LinearLayoutManager(this)
        binding.recyclerConversas.adapter = adapter

        binding.btnEnviar.setOnClickListener {
            val texto = binding.edtMensagem.text.toString().trim()
            if (!TextUtils.isEmpty(texto)) {
                enviarMensagem(texto)
            }
        }

        ouvirMensagens()
        val pedidoRef = FirebaseDatabase.getInstance()
            .getReference("conversas_clientes")
            .child(uidCliente)
            .child(uidPedido)

        pedidoRef.child("status").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)?.trim()?.lowercase()
                if (status == "finalizada") {
                    // Oculta campo de envio
                    binding.layoutMensagem.visibility = View.GONE

                    // Exibe aviso
                    binding.textoFinalizado.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

    fun setavolvar() {
        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun enviarMensagem(texto: String) {
        val timestamp = System.currentTimeMillis().toString()
        val mensagem = Mensagem(
            mensagem = texto,
            autor = "cliente",
            horario = timestamp
        )


        mensagensRef.child(timestamp).setValue(mensagem)
            .addOnSuccessListener {
                binding.edtMensagem.setText("")
                binding.recyclerConversas.scrollToPosition(listaMensagens.size - 1)
            }
    }

    private fun ouvirMensagens() {
        mensagensRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaMensagens.clear()
                for (mensagemSnap in snapshot.children) {
                    val msg = mensagemSnap.getValue(Mensagem::class.java)
                    msg?.let { listaMensagens.add(it) }
                }
                adapter.notifyDataSetChanged()
                binding.recyclerConversas.scrollToPosition(listaMensagens.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, "Erro ao carregar mensagens", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}

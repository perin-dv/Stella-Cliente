package com.example.apkstelladitalia20.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.databinding.ActivityInfoAcessoBinding
import com.google.firebase.database.*

class InfoAcessoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInfoAcessoBinding
    private lateinit var database: DatabaseReference
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoAcessoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("appStella", MODE_PRIVATE)
        uid = prefs.getString("uidCliente", null) ?: run {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database = FirebaseDatabase.getInstance().getReference("clientes")

        carregarDados()

        binding.btnAtualizar.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            val telefone = binding.editTelefone.text.toString().trim()

            if (email.isEmpty() || telefone.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dados = mapOf("email" to email, "telefone" to telefone)
            database.child(uid).updateChildren(dados).addOnSuccessListener {
                Toast.makeText(this, "Dados atualizados!", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Erro ao atualizar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun carregarDados() {
        database.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.editEmail.setText(snapshot.child("email").getValue(String::class.java))
                binding.editTelefone.setText(snapshot.child("telefone").getValue(String::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@InfoAcessoActivity, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

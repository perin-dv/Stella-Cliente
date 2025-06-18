// InfoPessoaisActivity.kt
package com.example.apkstelladitalia20.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.databinding.ActivityInfoPessoaisBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class InfoPessoaisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoPessoaisBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoPessoaisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val prefs = getSharedPreferences("appStella", MODE_PRIVATE)
        val uid = prefs.getString("uidCliente", null)

        if (uid.isNullOrEmpty()) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        val ref = FirebaseDatabase.getInstance().getReference("clientes/$uid")

        ref.get().addOnSuccessListener { snapshot ->
            binding.editNome.setText(snapshot.child("nome").value?.toString() ?: "")
            binding.editCpf.setText(snapshot.child("cpf").value?.toString() ?: "")
        }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
            }

        binding.btnAtualizar.setOnClickListener {
            val nome = binding.editNome.text.toString().trim()
            val cpf = binding.editCpf.text.toString().trim()

            if (nome.isEmpty() || cpf.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dados = mapOf("nome" to nome, "cpf" to cpf)
            ref.updateChildren(dados).addOnSuccessListener {
                Toast.makeText(this, "Dados atualizados com sucesso", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Erro ao atualizar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
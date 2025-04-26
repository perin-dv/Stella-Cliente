package com.example.apkstelladitalia20.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.databinding.ActivityLoginBinding
import com.example.apkstelladitalia20.helper.FirebaseHelper
import com.example.apkstelladitalia20.ui.login.CadastroActivity
import com.example.stelladitalia20.Entity.ClienteEntity
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val emailInput = binding.editEmail.text.toString().trim().lowercase()
            val senhaInput = binding.editSenha.text.toString().trim()
            if (emailInput.isEmpty() || senhaInput.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseHelper.database
                .child("clientes")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var clienteEncontrado: ClienteEntity? = null
                        for (child in snapshot.children) {
                            val c = child.getValue(ClienteEntity::class.java)
                            if (c != null && c.email.trim().lowercase() == emailInput) {
                                if (c.senha.trim() == senhaInput) {
                                    clienteEncontrado = c
                                } else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Senha incorreta",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return
                                }
                                break
                            }
                        }

                        if (clienteEncontrado != null) {
                            val prefs = getSharedPreferences("appStella", Context.MODE_PRIVATE)
                            with(prefs.edit()) {
                                putString("uidCliente", clienteEncontrado.uid) // salva UID do cliente
                                putString("nome", clienteEncontrado.nome)
                                putString("uidEmpresa", "7a3118oNdgcpmwSqrgyRTqBnFFx2") // salva UID fixo da empresa
                                apply()
                            }

                            Toast.makeText(
                                this@LoginActivity,
                                "Login realizado com sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            intent.putExtra("nomeCliente", clienteEncontrado.nome)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "Cliente não possui cadastro",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Erro ao acessar banco de dados",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }

        binding.textCadastro.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
    }
}

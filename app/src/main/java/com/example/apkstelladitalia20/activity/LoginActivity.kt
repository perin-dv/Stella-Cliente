package com.example.apkstelladitalia20.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.Entity.ClienteFirebase
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.databinding.ActivityLoginBinding
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 👁 Função mostrar/ocultar senha
        var senhaVisivel = false
        binding.btnToggleSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                binding.editSenha.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.btnToggleSenha.setImageResource(com.example.apkstelladitalia20.R.drawable.ic_eye_open)
            } else {
                binding.editSenha.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.btnToggleSenha.setImageResource(R.drawable.ic_eye_closed)
            }
            binding.editSenha.setSelection(binding.editSenha.text.length)
        }

        binding.textCadastro.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
            finish()
        }
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
                        var clienteEncontrado: ClienteFirebase? = null

                        for (child in snapshot.children) {
                            val c = try {
                                child.getValue(ClienteFirebase::class.java)
                            } catch (e: Exception) {
                                Log.e("LoginActivity", "Erro ao converter cliente: ${e.message}")
                                null
                            }

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
                                putString("uidCliente", clienteEncontrado.uid)
                                putString("nome", clienteEncontrado.nome)
                                putString("uidEmpresa", "7a3118oNdgcpmwSqrgyRTqBnFFx2")
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

            binding.textCadastro.setOnClickListener {
                startActivity(Intent(this, CadastroActivity::class.java))
            }
        }
    }
}
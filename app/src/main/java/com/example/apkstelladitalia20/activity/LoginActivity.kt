package com.example.apkstelladitalia20.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.Entity.ClienteFirebase
import com.example.apkstelladitalia20.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        FirebaseApp.initializeApp(this)

        var senhaVisivel = false
        binding.btnToggleSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            binding.editSenha.inputType = if (senhaVisivel)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            binding.editSenha.setSelection(binding.editSenha.text.length)
            binding.btnToggleSenha.setImageResource(
                if (senhaVisivel)
                    com.example.apkstelladitalia20.R.drawable.ic_eye_open
                else
                    com.example.apkstelladitalia20.R.drawable.ic_eye_closed
            )
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

            auth.signInWithEmailAndPassword(emailInput, senhaInput)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: return@addOnSuccessListener

                    // Agora busca os dados no Realtime Database
                    FirebaseDatabase.getInstance().getReference("clientes")
                        .child(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val cliente = snapshot.getValue(ClienteFirebase::class.java)
                                if (cliente != null) {
                                    val prefs =
                                        getSharedPreferences("appStella", Context.MODE_PRIVATE)
                                    prefs.edit()
                                        .putString("uidCliente", uid)
                                        .putString("nome", cliente.nome)
                                        .putString("emailCliente", cliente.email)
                                        .putString("senhaCliente", senhaInput)
                                        .putString("uidEmpresa", "7a3118oNdgcpmwSqrgyRTqBnFFx2")
                                        .apply()

                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Login realizado com sucesso!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(
                                        Intent(
                                            this@LoginActivity,
                                            HomeActivity::class.java
                                        )
                                    )
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Dados do cliente não encontrados",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Erro ao carregar dados",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao autenticar: ${it.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }
}

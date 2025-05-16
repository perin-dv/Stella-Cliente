package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.Entity.ClienteFirebase
import com.example.apkstelladitalia20.Entity.EnderecoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.databinding.ActivityCadastroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnCadastrar.setOnClickListener {
            val nome = binding.editNome.text?.toString()?.trim() ?: ""
            val emailInput = binding.editEmail.text.toString()
            Log.d("BINDING", "Email lido: $emailInput")
            val senha = binding.editSenha.text?.toString()?.trim() ?: ""
            val confirmarSenha = binding.editConfirmarSenha.text?.toString()?.trim() ?: ""

            if (nome.isEmpty() || emailInput.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senha != confirmarSenha) {
                binding.editConfirmarSenha.error = "As senhas precisam ser idênticas"
                return@setOnClickListener
            } else {
                binding.editConfirmarSenha.error = null
            }

            cadastrarUsuario(nome, emailInput, senha)
        }

        var senhaVisivel = false
        binding.btnToggleSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                binding.editSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.btnToggleSenha.setImageResource(R.drawable.ic_eye_open)
            } else {
                binding.editSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.btnToggleSenha.setImageResource(R.drawable.ic_eye_closed)
            }
            binding.editSenha.setSelection(binding.editSenha.text.length)
        }

        var confirmarSenhaVisivel = false
        binding.btnToggleConfirmarSenha.setOnClickListener {
            confirmarSenhaVisivel = !confirmarSenhaVisivel
            if (confirmarSenhaVisivel) {
                binding.editConfirmarSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.btnToggleConfirmarSenha.setImageResource(R.drawable.ic_eye_open)
            } else {
                binding.editConfirmarSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.btnToggleConfirmarSenha.setImageResource(R.drawable.ic_eye_closed)
            }
            binding.editConfirmarSenha.setSelection(binding.editConfirmarSenha.text.length)
        }
    }

    private fun cadastrarUsuario(nome: String, emailInput: String, senha: String) {
        auth.createUserWithEmailAndPassword(emailInput, senha)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: return@addOnSuccessListener
                val emailFinal = emailInput

                Log.d("CadastroDebug", "nome=$nome | email=$emailFinal | senha=$senha")

                val cliente = ClienteFirebase(
                    uid = uid,
                    nome = nome,
                    email = emailFinal,
                    endereco = EnderecoEntity(),
                    telefone = "",
                    senha = senha
                )

                FirebaseDatabase.getInstance()
                    .getReference("clientes")
                    .child(uid)
                    .setValue(cliente)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, DadosEntregaActivity::class.java)
                        intent.putExtra("nome", nome)
                        intent.putExtra("email", emailFinal)
                        intent.putExtra("senha", senha)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro ao salvar cliente no banco", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao cadastrar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

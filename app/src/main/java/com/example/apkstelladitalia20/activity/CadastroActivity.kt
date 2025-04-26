package com.example.apkstelladitalia20.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.databinding.ActivityCadastroBinding


class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCadastrar.setOnClickListener {
            val nome = binding.editNome.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val senha = binding.editSenha.text.toString().trim()
            val confirmar = binding.editConfirmarSenha.text.toString().trim()

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmar.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senha != confirmar) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, DadosEntregaActivity::class.java).apply {
                putExtra("nome", nome)
                putExtra("email", email)
                putExtra("senha", senha)
            }
            startActivity(intent)
        }
    }
}

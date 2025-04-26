package com.example.apkstelladitalia20.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.activity.HomeActivity
import com.example.apkstelladitalia20.databinding.ActivityDadosEntregaBinding
import com.example.apkstelladitalia20.helper.FirebaseHelper

import com.example.stelladitalia20.Entity.ClienteEntity
import com.example.stelladitaliaempresa.data.AppDatabase
import java.util.*


class DadosEntregaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDadosEntregaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDadosEntregaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnConcluirCadastro.setOnClickListener {
            val rua = binding.editRua.text.toString().trim()
            val numero = binding.editNumero.text.toString().trim()
            val bairro = binding.editBairro.text.toString().trim()
            val cidade = binding.editCidade.text.toString().trim()
            val uf = binding.editUf.text.toString().trim()
            val telefone = binding.editTelefone.text.toString().trim()
            val referencia = binding.editReferencia.text.toString().trim()

            if (rua.isEmpty() || numero.isEmpty() || bairro.isEmpty() || cidade.isEmpty() || uf.isEmpty() || telefone.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val enderecoCompleto = "$rua, $numero - $bairro, $cidade/$uf"
            val nome = intent.getStringExtra("nome") ?: ""
            val email = intent.getStringExtra("email") ?: ""
            val senha = intent.getStringExtra("senha") ?: ""
            val id = UUID.randomUUID().toString()

            val cliente = ClienteEntity(
                uid = id,
                nome = nome,
                email = email,
                senha = senha,
                endereco = "$enderecoCompleto (${referencia.ifEmpty { "Sem referência" }})",
                telefone = telefone,
                           )

            FirebaseHelper.database
                .child("clientes")
                .child(id)
                .setValue(cliente)
                .addOnSuccessListener {
                    Thread {
                        AppDatabase.getDatabase(this).clienteDao().inserir(cliente)
                    }.start()

                    Toast.makeText(this, "Cadastro concluído com sucesso!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finishAffinity()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao cadastrar", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

package com.example.apkstelladitalia20.activity

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.Entity.ClienteFirebase
import com.example.apkstelladitalia20.Entity.EnderecoEntity
import com.example.apkstelladitalia20.databinding.ActivityDadosEntregaBinding
import com.example.stelladitalia20.Entity.ClienteEntity

class DadosEntregaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDadosEntregaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDadosEntregaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carregarDadosExistentes()


        binding.btnConcluirCadastro.setOnClickListener {
            validarDados()
        }

        binding.editCep.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val cep = binding.editCep.text.toString().trim()
                if (cep.length == 8) {
                    buscarEnderecoPorCep(cep, this) { rua, bairro, cidade, uf ->
                        binding.editRua.setText(rua)
                        binding.editBairro.setText(bairro)
                        binding.editCidade.setText(cidade)
                        binding.editUf.setText(uf)
                    }
                }
            }
        }

    }

    private fun carregarDadosExistentes() {
        val uid = FirebaseHelper.getIdUsuario() ?: return
        val ref = FirebaseHelper.database.child("clientes").child(uid)

        ref.get().addOnSuccessListener { snapshot ->
            snapshot.getValue(ClienteEntity::class.java)?.let { cliente ->
                binding.editTelefone.setText(cliente.telefone)
                binding.editRua.setText(cliente.endereco?.rua?:"")
                binding.editBairro.setText(cliente.endereco?.bairro?:"")
                binding.editReferencia.setText(cliente.endereco?.referencia?:"")
            }
        }
    }
    private fun validarDados() {
        val telefone = binding.editTelefone.text.toString().trim()
        val rua = binding.editRua.text.toString().trim()
        val numero = binding.editNumero.text.toString().trim()
        val bairro = binding.editBairro.text.toString().trim()
        val cidade = binding.editCidade.text.toString().trim()
        val estado = binding.editUf.text.toString().trim()
        val cep = binding.editCep.text.toString().trim()
             val referencia = binding.editReferencia.text.toString().trim()

        if (telefone.isEmpty() || rua.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
            return
        }

        val enderecoCompleto = EnderecoEntity(
            rua = rua,
            numero = numero,
            bairro = bairro,
            cidade = cidade,
            estado = estado,
            cep = cep,
            referencia = referencia
        )

        val nome = intent.getStringExtra("nome") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val senha = intent.getStringExtra("senha") ?: ""
        val cliente = ClienteFirebase(
            uid = FirebaseHelper.getIdUsuario() ?: "",
            nome = nome,
            email = email,
            senha = senha,
            telefone = telefone,
            endereco = enderecoCompleto
        )
        salvarCliente(cliente)
    }

    fun buscarEnderecoPorCep(cep: String, context: Context, onResult: (rua: String, bairro: String, cidade: String, uf: String) -> Unit) {
        val geocoder = Geocoder(context)
        try {
            val addresses = geocoder.getFromLocationName(cep, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val rua = address.thoroughfare ?: ""
                val bairro = address.subLocality ?: ""
                val cidade = address.locality ?: ""
                val uf = address.adminArea ?: ""
                onResult(rua, bairro, cidade, uf)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun salvarCliente(cliente: ClienteFirebase) {
        val uid = FirebaseHelper.getIdUsuario() ?: return

        // 1. Salva os dados do cliente (sem o endereço embutido)
        val clienteSemEndereco = cliente.copy(endereco = null)

        FirebaseHelper.database
            .child("clientes")
            .child(uid)
            .setValue(clienteSemEndereco)
            .addOnSuccessListener {

                // 2. Salva o endereço separadamente na lista "enderecos"
                val enderecoRef = FirebaseHelper.database
                    .child("clientes")
                    .child(uid)
                    .child("enderecos")
                    .push()

                enderecoRef.setValue(cliente.endereco)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cadastro salvo com sucesso ✅", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro ao salvar o endereço 😢", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar o cliente 😢", Toast.LENGTH_SHORT).show()
            }
    }

}

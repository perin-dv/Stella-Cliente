package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.databinding.ActivityEnderecoEntregaBinding
import com.example.apkstelladitalia20.helper.setupToolbar

class EnderecoEntregaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnderecoEntregaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnderecoEntregaBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupToolbar(binding.includeToolbar)

        setupClicks()
        preencherEndereco()

    }


    private fun setupClicks() {
        binding.btnContinuarEndereco.setOnClickListener {
            startActivity(Intent(this, ResumoPedidoActivity::class.java))
        }

        binding.txtTrocarEndereco.setOnClickListener {
            // Aqui depois podemos abrir uma tela de busca de endereço ou mapa
            // Por enquanto só mostra um toast ou mantém como placeholder
        }
    }

    private fun preencherEndereco() {
        // Aqui simula puxar o endereço do Firebase ou Room (já cadastrado do usuário)
        // Depois podemos puxar dinâmico do banco se quiser

        binding.txtEnderecoCompleto.text = "Rua São João, 1701"
        binding.txtDescricaoEntrega.text = "Próximo ao mercado"
    }
}

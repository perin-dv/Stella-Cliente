package com.example.apkstelladitalia20.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.databinding.FragmentCarrinhoBinding

class CarrinhoActivity : AppCompatActivity() {

    private lateinit var binding: FragmentCarrinhoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentCarrinhoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupResumoInicial()
        setupBotaoContinuar()
    }

    private fun setupResumoInicial() {
        binding.txtSubtotal.text = "R$ 0,00"
        binding.txtTaxaEntrega.text = "R$ 5,00"
        binding.txtTotal.text = "R$ 5,00"
    }

    private fun setupBotaoContinuar() {
        binding.btnContinuar.setOnClickListener {
            // Aqui você pode seguir para a próxima etapa do pedido
            // Exemplo: abrir endereço de entrega
        }
    }
}

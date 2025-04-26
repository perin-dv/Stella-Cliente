package com.example.apkstelladitalia20.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.databinding.ActivityCheckoutBinding

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val enderecoEntrega = intent.getStringExtra("enderecoEntrega") ?: "Endereço não encontrado"
        binding.tvEnderecoEntrega.text = enderecoEntrega

        // Aqui você pode continuar o processo de confirmação do pedido
        // Ex: botão de finalizar, cálculo de taxa de entrega, etc.

        binding.btnConfirmarPedido.setOnClickListener {
            // Enviar pedido com o endereço, ou salvar no Firebase
        }
    }
}

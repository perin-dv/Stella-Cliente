package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.databinding.ActivityEnderecoEntregaBinding
import com.example.apkstelladitalia20.fragment.EnderecoDialogFragment
import com.example.apkstelladitalia20.helper.setupToolbar
import com.example.apkstelladitalia20.model.PizzaResumo
import com.example.apkstelladitalia20.ui.carrinho.CarrinhoFragment

class EnderecoEntregaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnderecoEntregaBinding
    private var pedido: PedidoEntity? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnderecoEntregaBinding.inflate(layoutInflater)
        setContentView(binding.root)


        pedido = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("pedidoTemp", PedidoEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<PedidoEntity>("pedidoTemp")
        }

        if (pedido == null) {
            Toast.makeText(this, "Pedido inválido (etapa endereço)", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("PEDIDO_DEBUG", "Recebido no EnderecoEntrega: $pedido")
        setupToolbar(binding.includeToolbar)

        setupClicks()
        preencherEndereco()

        binding.txtTrocarEndereco.setOnClickListener {
            val enderecoDialog = EnderecoDialogFragment(this) { enderecoSelecionado ->
                binding.txtTrocarEndereco.text = enderecoSelecionado
            }
            enderecoDialog.show(supportFragmentManager, "EnderecoDialog")
        }

    }

    private fun setupClicks() {
        binding.btnContinuarEndereco.setOnClickListener {
            val intent = Intent(this, ResumoPedidoProdutoActivity::class.java)
            intent.putExtra("pedidoTemp", pedido)
            startActivity(intent)
        }

        }

    private fun preencherEndereco() {
        binding.txtEnderecoCompleto.text = "Rua São João, 1701"
        binding.txtDescricaoEntrega.text = "Próximo ao mercado"
    }
}

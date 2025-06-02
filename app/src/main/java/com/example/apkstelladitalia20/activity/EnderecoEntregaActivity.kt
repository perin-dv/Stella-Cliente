package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.databinding.ActivityEnderecoEntregaBinding
import com.example.apkstelladitalia20.fragment.EnderecoDialogFragment
import com.example.apkstelladitalia20.helper.setupToolbar
import com.example.apkstelladitalia20.model.PizzaResumo
import com.example.apkstelladitalia20.ui.carrinho.CarrinhoFragment

class EnderecoEntregaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnderecoEntregaBinding
    private var pizzaResumo: PizzaResumo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnderecoEntregaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.includeToolbar)
        pizzaResumo = intent.getParcelableExtra("pizzaResumo")
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
            intent.putExtra("pizzaResumo", pizzaResumo)
            startActivity(intent)
        }
    }

    private fun preencherEndereco() {
        binding.txtEnderecoCompleto.text = "Rua São João, 1701"
        binding.txtDescricaoEntrega.text = "Próximo ao mercado"
    }
}

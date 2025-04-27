package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.adapter.BebidaAdapter
import com.example.apkstelladitalia20.adapter.CarrinhoPromocaoAdapter
import com.example.apkstelladitalia20.databinding.ActivityCarrinhoPromocaoBinding
import com.example.apkstelladitalia20.helper.setupToolbar
import com.example.apkstelladitalia20.model.BebidaEntity
import com.example.apkstelladitalia20.model.PromocaoEntity

class CarrinhoPromocaoActivity : AppCompatActivity(), CarrinhoPromocaoAdapter.CarrinhoListener {

    private lateinit var binding: ActivityCarrinhoPromocaoBinding
    private lateinit var carrinhoAdapter: CarrinhoPromocaoAdapter
    private lateinit var bebidaAdapter: BebidaAdapter


    private val listaCarrinho = mutableListOf<PromocaoEntity>()
    private val listaBebidas = mutableListOf<BebidaEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarrinhoPromocaoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.includeToolbar)

        setupCarrinhoRecycler()
        setupBebidaRecycler()
        carregarBebidasMock()
        carregarCarrinhoMock()


        binding.btnContinuar.setOnClickListener {
            Toast.makeText(this, "Continuando para Entrega...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ResumoPedidoActivity::class.java)
            intent.putExtra("carrinhoSelecionado", ArrayList(listaCarrinho))
            startActivity(intent)

        }


        binding.recyclerItensCarrinho.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.recyclerBebidas.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )



    }



    private fun setupCarrinhoRecycler() {
        carrinhoAdapter = CarrinhoPromocaoAdapter(listaCarrinho, object : CarrinhoPromocaoAdapter.CarrinhoListener {
            override fun onQuantidadeAlterada() {
                atualizarResumo()
                binding.recyclerItensCarrinho.scheduleLayoutAnimation()
            }
        })

        binding.recyclerItensCarrinho.apply {
            layoutManager = LinearLayoutManager(this@CarrinhoPromocaoActivity)
            adapter = carrinhoAdapter
            layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)
        }
    }


    private fun setupBebidaRecycler() {
        bebidaAdapter = BebidaAdapter(listaBebidas) { bebida ->
            adicionarBebidaAoCarrinho(bebida)
        }
        binding.recyclerBebidas.apply {
            layoutManager = LinearLayoutManager(
                this@CarrinhoPromocaoActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = bebidaAdapter
        }
    }


    private fun carregarBebidasMock() {
        listaBebidas.addAll(
            listOf(
                BebidaEntity("Coca-Cola 2L", 12.99),
                BebidaEntity("Guaraná 1L", 9.50),
                BebidaEntity("Suco Natural", 10.00)
            )
        )
        bebidaAdapter.notifyDataSetChanged()
    }

    private fun carregarCarrinhoMock() {
        // Exemplo inicial de um item de promoção já no carrinho
        val promocaoSelecionada = intent.getSerializableExtra("promocaoSelecionada") as? PromocaoEntity
        promocaoSelecionada?.let {
            listaCarrinho.add(it)
            carrinhoAdapter.notifyDataSetChanged()
            atualizarResumo()
        }

    }

    private fun adicionarBebidaAoCarrinho(bebida: BebidaEntity) {
        val bebidaPromocao = PromocaoEntity(
            id = bebida.nome,
            titulo = bebida.nome,
            valor = bebida.preco,
            quantidade = 1
        )
        listaCarrinho.add(bebidaPromocao)
        carrinhoAdapter.notifyDataSetChanged()
        atualizarResumo()
        Toast.makeText(this, "${bebida.nome} adicionado!", Toast.LENGTH_SHORT).show()
    }

    private fun atualizarResumo() {
        var subtotal = 0.0
        listaCarrinho.forEach { produto ->
            subtotal += (produto.valor ?: 0.0) * (produto.quantidade ?: 1)
        }
        val entrega = 5.00
        val total = subtotal + entrega

        binding.txtSubtotal.text = "Subtotal: R$ %.2f".format(subtotal)
        binding.txtTaxaEntrega.text = "Taxa de entrega: R$ %.2f".format(entrega)
        binding.txtTotal.text = "Total: R$ %.2f".format(total)
    }

    override fun onQuantidadeAlterada() {
        atualizarResumo()
    }

}

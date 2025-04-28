package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        // Botão voltar funcionando
        binding.includeToolbar.btnVoltar.setOnClickListener {
            finish()
        }

        setupCarrinhoRecycler()
        setupBebidaRecycler()

        receberPromocaoSelecionada()

        binding.btnContinuar.setOnClickListener {
            if (listaCarrinho.isNotEmpty()) {
                val intent = Intent(this, ResumoPedidoActivity::class.java)
                intent.putExtra("carrinhoSelecionado", ArrayList(listaCarrinho))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Seu carrinho está vazio!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.txtAdicionarMaisItens2.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
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
            layoutManager = LinearLayoutManager(this@CarrinhoPromocaoActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = bebidaAdapter
        }
        carregarBebidasMock()
    }

    private fun carregarBebidasMock() {
        listaBebidas.addAll(
            listOf(
                BebidaEntity("Pizza Calabresa", 49.90),
                BebidaEntity("Pizza 4 Queijos", 54.90),
                BebidaEntity("Porção de Batata Frita", 25.00),
                BebidaEntity("Lasanha Bolonhesa", 42.00),
                BebidaEntity("Pizza Portuguesa", 50.00),
                BebidaEntity("Calzone Presunto Queijo", 35.00),
                BebidaEntity("Pizza Frango Catupiry", 53.90),
                BebidaEntity("Porção de Mandioca", 27.00),
                BebidaEntity("Pizza Vegetariana", 48.00)
            )
        )
        bebidaAdapter.notifyDataSetChanged()
    }

    private fun receberPromocaoSelecionada() {
        val promocaoSelecionada = intent.getSerializableExtra("promocaoSelecionada") as? PromocaoEntity
        if (promocaoSelecionada != null) {
            preencherCarrinho(promocaoSelecionada)
        } else {
            Toast.makeText(this, "Erro ao carregar promoção.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun preencherCarrinho(promocao: PromocaoEntity) {
        listaCarrinho.clear()
        listaCarrinho.add(promocao)
        carrinhoAdapter.notifyDataSetChanged()
        atualizarResumo()
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

        binding.txtSubtotal.text = "R$ %.2f".format(subtotal)
        binding.txtTaxaEntrega.text = if (entrega == 0.0) "Grátis" else "R$ %.2f".format(entrega)
        binding.txtTotal.text = "R$ %.2f".format(total)
    }

    override fun onQuantidadeAlterada() {
        atualizarResumo()
    }
}

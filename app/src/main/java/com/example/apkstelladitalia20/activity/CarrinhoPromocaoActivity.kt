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
import com.google.firebase.firestore.FirebaseFirestore

class CarrinhoPromocaoActivity : AppCompatActivity(), CarrinhoPromocaoAdapter.CarrinhoListener {

    private lateinit var binding: ActivityCarrinhoPromocaoBinding
    private lateinit var carrinhoAdapter: CarrinhoPromocaoAdapter
    private lateinit var bebidaAdapter: BebidaAdapter
    private var idPromocaoSelecionada: String? = null

    private val listaCarrinho = mutableListOf<PromocaoEntity>()
    private val listaBebidas = mutableListOf<BebidaEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarrinhoPromocaoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.includeToolbar)


        carregarResumoDoCarrinho()
        setupCarrinhoRecycler()
        setupBebidaRecycler()
        carregarBebidasMock()



        idPromocaoSelecionada = intent.getStringExtra("idPromocaoSelecionada")
        if (idPromocaoSelecionada != null) {
            buscarPromocaoFirebase(idPromocaoSelecionada!!)
        }

        binding.btnContinuar.setOnClickListener {
            Toast.makeText(this, "Continuando para Entrega...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, CarrinhoPromocaoActivity::class.java)
            intent.putExtra("idPromocaoSelecionada", promocao.id)
            startActivity(intent)

        }

        binding.txtAdicionarMaisItens2.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java) // ou sua HomeActivity real
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // limpar stack e voltar limpo
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

    private fun buscarPromocaoFirebase(idPromocao: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("promocoes").document(idPromocao)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val promocao = document.toObject(PromocaoEntity::class.java)
                    if (promocao != null) {
                        preencherCarrinho(promocao)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao buscar promoção.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun carregarResumoDoCarrinho() {
        val listaCarrinho = intent.getSerializableExtra("carrinhoSelecionado") as? ArrayList<PromocaoEntity>

        if (!listaCarrinho.isNullOrEmpty()) {
            var subtotal = 0.0

            listaCarrinho.forEach { item ->
                subtotal += (item.valor ?: 0.0) * (item.quantidade ?: 1)
            }

            val taxaEntrega = 5.0 // ou 0.0 se quiser grátis
            val total = subtotal + taxaEntrega

            binding.txtSubtotal.text = "R$ %.2f".format(subtotal)
            binding.txtTaxaEntrega.text = if (taxaEntrega == 0.0) "Grátis" else "R$ %.2f".format(taxaEntrega)
            binding.txtTotal.text = "R$ %.2f".format(total)
        } else {
            Toast.makeText(this, "Nenhum item encontrado no carrinho.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun preencherCarrinho(promocao: PromocaoEntity) {
        // Atualiza seu adapter do carrinho aqui
        listaCarrinho.clear()
        listaCarrinho.add(promocao)
        carrinhoAdapter.notifyDataSetChanged()
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

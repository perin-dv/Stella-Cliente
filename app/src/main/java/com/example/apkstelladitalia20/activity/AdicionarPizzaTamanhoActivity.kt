package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.adapter.SaborAdapter
import com.example.apkstelladitalia20.databinding.ActivityAdicionarPizzaTamanhoBinding
import com.example.apkstelladitalia20.model.CarrinhoViewModel
import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity
import com.example.apkstelladitalia20.model.SaborEntity
import com.google.firebase.database.*

class AdicionarPizzaTamanhoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdicionarPizzaTamanhoBinding
    private var precoBase = 0.0

    private var saborSelecionado1: SaborEntity? = null
    private var saborSelecionado2: SaborEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdicionarPizzaTamanhoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        botaoFinalizar()
        dadosRecebidos()
    }

    private fun dadosRecebidos() {
        val nome = intent.getStringExtra("tamanhoNome") ?: "Pizza"
        val descricao = intent.getStringExtra("descricao") ?: ""
        val preco = intent.getStringExtra("precoBase") ?: "R$ 0,00"
        val imagemUrl = intent.getStringExtra("imagemUrl") ?: ""

        binding.tvTituloPizza.text = "$nome ($descricao)"
        binding.tvPrecoBase.text = "A partir de $preco"
        binding.tvTotal.text = "Total: $preco"

        Glide.with(this)
            .load(imagemUrl)
            .placeholder(R.drawable.ic_pizza)
            .into(binding.imgPizzaTopo)

        precoBase = preco.replace("R$", "")
            .replace(",", ".")
            .trim()
            .toDoubleOrNull() ?: 0.0

        carregarSabores(nome)
    }

    private fun carregarSabores(tamanho: String) {
        val uidEmpresa = "7a3118oNdgcpmwSqrgyRTqBnFFx2"

        val ref = FirebaseDatabase.getInstance()
            .getReference("empresa")
            .child(uidEmpresa)
            .child("produtos")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaSabores = mutableListOf<SaborEntity>()

                for (prodSnap in snapshot.children) {
                    val nome = prodSnap.child("nome").getValue(String::class.java) ?: continue
                    val categoria = prodSnap.child("categoria").getValue(String::class.java) ?: ""
                    val preco = prodSnap.child("preco").getValue(Double::class.java) ?: 0.0
                    val descricao = prodSnap.child("descricao").getValue(String::class.java) ?: ""
                    val imagem = prodSnap.child("imagem").getValue(String::class.java) ?: ""

                    val tipo = intent.getStringExtra("tipo") ?: "salgada"

                    val categoriaOk = if (tipo == "doce") {
                        categoria.contains("doce", ignoreCase = true)
                    } else {
                        categoria.contains("Pizza", true) &&
                                !categoria.contains("Doce", true) &&
                                !categoria.contains("Porção", true) &&
                                !categoria.contains("Bebida", true)
                    }


                    if (categoriaOk) {
                        val sabor = SaborEntity(
                            nome = nome,
                            descricao = descricao,
                            precoAdicional = preco,
                            imagem = imagem
                        )
                        listaSabores.add(sabor)
                    }
                }

                setupListaSabores(listaSabores)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE_SABORES", "Erro: ${error.message}")
            }
        })
    }

    private fun setupListaSabores(lista: List<SaborEntity>) {
        val adapter1 = SaborAdapter(lista) { sabor ->
            saborSelecionado1 = sabor
            binding.scrollView.post {
                binding.scrollView.smoothScrollTo(0, binding.tituloEscolha2.top)
            }
        }

        val adapter2 = SaborAdapter(lista) { sabor ->
            saborSelecionado2 = sabor
            atualizarTotal()
        }

        binding.rvEscolha1.layoutManager = LinearLayoutManager(this)
        binding.rvEscolha1.adapter = adapter1

        binding.rvEscolha2.layoutManager = LinearLayoutManager(this)
        binding.rvEscolha2.adapter = adapter2
    }

    private fun atualizarTotal() {
        val preco1 = saborSelecionado1?.precoAdicional ?: 0.0
        val preco2 = saborSelecionado2?.precoAdicional ?: 0.0
        val adicional = maxOf(preco1, preco2)
        val total = precoBase + adicional

        binding.tvTotal.text = "Total: R$ %.2f".format(total)
    }

    private fun botaoFinalizar() {
        binding.btnAdicionar.setOnClickListener {
            if (saborSelecionado1 == null || saborSelecionado2 == null) {
                Toast.makeText(this, "Escolha os 2 sabores da pizza", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val preco1 = saborSelecionado1?.precoAdicional ?: 0.0
            val preco2 = saborSelecionado2?.precoAdicional ?: 0.0
            val adicional = maxOf(preco1, preco2)
            val total = precoBase + adicional

            val item = ProdutoCarrinhoEntity(
                idProduto = "pizza_${System.currentTimeMillis()}",
                nome = "${saborSelecionado1?.nome} + ${saborSelecionado2?.nome}",
                valor = total,
                quantidade = 1,
                tipo = "pizza",
                descricao = binding.tvTituloPizza.text.toString(),
                imagemUrl = null
            )

            val viewModel = ViewModelProvider(this)[CarrinhoViewModel::class.java]
            viewModel.adicionar(item)

            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("abrirCarrinho", true)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}
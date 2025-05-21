package com.example.apkstelladitalia20.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.apkstelladitalia20.adapter.SaborAdapter
import com.example.apkstelladitalia20.databinding.ActivityAdicionarPizzaTamanhoBinding
import com.example.apkstelladitalia20.model.SaborEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.apkstelladitalia20.R

class AdicionarPizzaTamanhoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdicionarPizzaTamanhoBinding
    private var precoBase = 0.0

    private var saborSelecionado1: SaborEntity? = null
    private var saborSelecionado2: SaborEntity? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdicionarPizzaTamanhoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dados recebidos
        val nome = intent.getStringExtra("tamanhoNome") ?: "Pizza"
        val descricao = intent.getStringExtra("descricao") ?: ""
        val preco = intent.getStringExtra("precoBase") ?: "R$ 0,00"
        val imagemUrl = intent.getStringExtra("imagemUrl") ?: ""

        // UI
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

        // Carrega sabores reais do Firebase
        carregarSabores(nome)
    }

    private fun carregarSabores(tamanho: String) {
        val uidEmpresa = "7a3118oNdgcpmwSqrgyRTqBnFFx2" // ou pegue do prefs, se quiser dinâmico

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

                    // 🍕 Filtro: só categorias salgadas válidas
                    val categoriaOk = categoria.contains("Pizza", true) &&
                            !categoria.contains("Doce", true) &&
                            !categoria.contains("Porção", true) &&
                            !categoria.contains("Bebida", true)

                    // 🍕 Verifica se nome inclui o tamanho desejado
                    val tamanhoOk = true

                    if (categoriaOk && tamanhoOk) {
                        val sabor = SaborEntity(
                            nome = nome,
                            descricao = descricao,
                            precoAdicional = preco,
                            imagem = imagem
                        )
                        listaSabores.add(sabor)
                    }
                }

                // Agora envia pra função que exibe os 2 RVs
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
}

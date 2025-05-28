package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.databinding.ActivityCardapioPorCategoriBinding
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.google.firebase.database.*
import com.stelladitalia.adapters.ProdutoAdapter

class CardapioPorCategoriaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCardapioPorCategoriBinding
    private lateinit var produtoAdapter: ProdutoAdapter
    private lateinit var categoriaSelecionada: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardapioPorCategoriBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoriaSelecionada = intent.getStringExtra("categoria")?.trim() ?: ""
        binding.tituloCategoria.text = categoriaSelecionada

        produtoAdapter = ProdutoAdapter(this, emptyList()) { produto ->
            val intent = Intent(this, DetalhesProdutoActivity::class.java)
            intent.putExtra("produtoId", produto.id)
            startActivity(intent)
        }

        binding.recyclerProdutos.adapter = produtoAdapter
        binding.recyclerProdutos.layoutManager = LinearLayoutManager(this)

        carregarProdutos()
    }

    private fun carregarProdutos() {
        val uidEmpresa = "7a3118oNdgcpmwSqrgyRTqBnFFx2" // substitua por prefs.getString("uidEmpresa", "")
        val ref = FirebaseDatabase.getInstance()
            .getReference("empresa")
            .child(uidEmpresa)
            .child("produtos")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val produtos = mutableListOf<ProdutoEntity>()
                for (produtoSnap in snapshot.children) {
                    val produto = produtoSnap.getValue(ProdutoEntity::class.java)
                    Log.d("🔥 PRODUTO VERIFICADO", "${produto?.nome} | categoria='${produto?.categoria}' | idUsuario=${produto?.idUsuario}")
                    if (produto != null &&
                        produto.categoria?.contains(categoriaSelecionada, ignoreCase = true) == true
                        &&
                        produto.idUsuario == uidEmpresa) {
                        produtos.add(produto)
                    }
                }
                Log.d("🔥 RESULTADO FINAL", "Encontrados: ${produtos.size} produtos para categoria '$categoriaSelecionada'")
                produtoAdapter.atualizarLista(produtos)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@CardapioPorCategoriaActivity,
                    "Erro ao carregar produtos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}

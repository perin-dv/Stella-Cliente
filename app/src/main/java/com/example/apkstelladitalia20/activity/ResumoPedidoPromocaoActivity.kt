package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.MainActivity
import com.example.apkstelladitalia20.adapter.ResumoPedidoPromocaoAdapter
import com.example.apkstelladitalia20.bottomsheet.BottomSheetFormaPagamento
import com.example.apkstelladitalia20.databinding.ActivityResumoPedidoBinding
import com.example.apkstelladitalia20.helper.setupToolbar
import com.example.apkstelladitalia20.model.PromocaoEntity
import com.google.firebase.firestore.FirebaseFirestore

class ResumoPedidoPromocaoActivity : AppCompatActivity(), ResumoPedidoPromocaoAdapter.ResumoListener {

    private lateinit var binding: ActivityResumoPedidoBinding
    private var listaCarrinho: ArrayList<PromocaoEntity> = arrayListOf()
    private var subtotal = 0.0
    private var taxaEntrega = 0.0
    private var descontoCupom = 0.0
    private var cpfNaNota: String? = null
    private var formaPagamentoSelecionada: String? = null
    private var trocoPara: String? = null
    private lateinit var resumoAdapter: ResumoPedidoPromocaoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResumoPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.includeToolbar)
        setupListeners()
        setupRecyclerResumo()
        carregarResumoValores()
        carregarTaxaEntregaFirebase()
    }

    private fun setupListeners() {
        binding.btnConfirmarPedido.setOnClickListener {
            confirmarPedido()
        }

        binding.txtTrocarPagamento.setOnClickListener {
            mostrarBottomSheetPagamento()
        }

        binding.txtAdicionarCupom.setOnClickListener {
            aplicarCupom()
        }

        binding.txtCpfNota.setOnClickListener {
            aplicarCpfNaNota()
        }
    }

    private fun carregarResumoValores() {
        listaCarrinho = intent.getSerializableExtra("carrinhoSelecionado") as? ArrayList<PromocaoEntity> ?: arrayListOf()

        if (listaCarrinho.isNotEmpty()) {
            subtotal = listaCarrinho.sumOf { (it.valor ?: 0.0) * (it.quantidade ?: 1) }
            atualizarResumoValores()
        } else {
            Toast.makeText(this, "Nenhum item no carrinho.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun carregarTaxaEntregaFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("empresas").document("idEmpresa")
            .get()
            .addOnSuccessListener { document ->
                taxaEntrega = document.getDouble("taxadefrete") ?: 0.0
                atualizarResumoValores()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar taxa de entrega.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun aplicarCupom() {
        val cupomDigitado = binding.txtAdicionarCupom.text.toString().trim()
        if (cupomDigitado.equals("DESCONTO10", ignoreCase = true)) {
            descontoCupom = 10.0
            Toast.makeText(this, "Cupom aplicado com sucesso!", Toast.LENGTH_SHORT).show()
            atualizarResumoValores()
        } else if (cupomDigitado.equals("FRETEGRATIS", ignoreCase = true)) {
            taxaEntrega = 0.0
            Toast.makeText(this, "Frete gratuito aplicado!", Toast.LENGTH_SHORT).show()
            atualizarResumoValores()
        } else {
            Toast.makeText(this, "Cupom inválido.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun aplicarCpfNaNota() {
        val cpfDigitado = binding.txtCpfNota.text.toString().trim()
        if (cpfDigitado.length == 11) {
            cpfNaNota = cpfDigitado
            Toast.makeText(this, "CPF adicionado na nota.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "CPF inválido. Deve conter 11 dígitos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerResumo() {
        resumoAdapter = ResumoPedidoPromocaoAdapter(listaCarrinho, this)
        binding.recyclerResumoPedido.apply {
            layoutManager = LinearLayoutManager(this@ResumoPedidoPromocaoActivity)
            adapter = resumoAdapter
        }
    }

    private fun atualizarResumoValores() {
        val subtotalComDesconto = (subtotal - descontoCupom).coerceAtLeast(0.0)
        val total = subtotalComDesconto + taxaEntrega

        binding.txtSubtotalResumo.text = "R$ %.2f".format(subtotalComDesconto)
        binding.txtTaxaEntregaResumo.text = if (taxaEntrega == 0.0) "Grátis" else "R$ %.2f".format(taxaEntrega)
        binding.txtTotalResumo.text = "R$ %.2f".format(total)
    }

    private fun confirmarPedido() {
        Toast.makeText(this, "Pedido realizado com sucesso!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun mostrarBottomSheetPagamento() {
        val bottomSheet = BottomSheetFormaPagamento(
            onPagamentoSelecionado = { forma, troco ->
                formaPagamentoSelecionada = forma
                trocoPara = troco
            },
            pagamentoCallback = { forma, textoVisivel ->
                val pagamentoTexto: String =
                    if (forma == "Dinheiro" && !textoVisivel.isNullOrEmpty()) {
                        "Dinheiro"
                    } else {
                        forma
                    }
                binding.txtFormaPagamento.text = pagamentoTexto
            }
        )
    }

    override fun onResumoAtualizado() {
        atualizarResumoValores()
    }
}

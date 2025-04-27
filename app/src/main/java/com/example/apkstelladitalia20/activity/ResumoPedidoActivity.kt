package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.bottomsheet.BottomSheetFormaPagamento
import com.example.apkstelladitalia20.databinding.ActivityResumoPedidoBinding
import com.example.apkstelladitalia20.helper.setupToolbar
import com.example.apkstelladitalia20.model.PromocaoEntity
import com.google.firebase.firestore.FirebaseFirestore

class ResumoPedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResumoPedidoBinding
    private var listaCarrinho: ArrayList<PromocaoEntity> = arrayListOf()
    private var subtotal = 0.0
    private var taxaEntrega = 0.0
    private var descontoCupom = 0.0
    private var cpfNaNota: String? = null
    private var formaPagamentoSelecionada: String? = null
    private var trocoPara: String? = null
    private var idPromocaoSelecionada: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResumoPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.includeToolbar)
        setupListeners()
        carregarResumoValores()
        carregarTaxaEntregaFirebase()

        idPromocaoSelecionada = intent.getStringExtra("idPromocaoSelecionada")
        if (idPromocaoSelecionada != null) {
            buscarPromocaoResumoFirebase(idPromocaoSelecionada!!)
        }

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

    private fun atualizarResumoValores() {
        val subtotalComDesconto = (subtotal - descontoCupom).coerceAtLeast(0.0)
        val total = subtotalComDesconto + taxaEntrega

        binding.txtSubtotalResumo.text = "R$ %.2f".format(subtotalComDesconto)
        binding.txtTaxaEntregaResumo.text = if (taxaEntrega == 0.0) "Grátis" else "R$ %.2f".format(taxaEntrega)
        binding.txtTotalResumo.text = "R$ %.2f".format(total)
    }

    private fun confirmarPedido() {
        Toast.makeText(this, "Pedido confirmado com sucesso!", Toast.LENGTH_LONG).show()
        enviarNotificacaoEmpresa()

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun enviarNotificacaoEmpresa() {
        println("🔔 Empresa: Novo pedido aguardando confirmação!")
    }

    private fun mostrarBottomSheetPagamento() {
        val bottomSheet = BottomSheetFormaPagamento { formaPagamento, troco ->
            formaPagamentoSelecionada = formaPagamento
            trocoPara = troco

            val pagamentoTexto = if (formaPagamento == "Dinheiro" && !troco.isNullOrEmpty()) {
                "Dinheiro (Troco para R$$troco)"
            } else {
                formaPagamento
            }
            binding.txtFormaPagamento.text = pagamentoTexto
        }
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }
    private fun buscarPromocaoResumoFirebase(idPromocao: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("promocoes").document(idPromocao)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val promocao = document.toObject(PromocaoEntity::class.java)
                    if (promocao != null) {
                        listaCarrinho.clear()
                        listaCarrinho.add(promocao)
                        atualizarResumoValores()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao buscar promoção.", Toast.LENGTH_SHORT).show()
            }
    }
}

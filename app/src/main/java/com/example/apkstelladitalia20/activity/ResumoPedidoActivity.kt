package com.example.apkstelladitalia20.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.Entity.EnderecoEntity
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.databinding.ActivityResumoPedidoBinding
import com.example.apkstelladitalia20.helper.setupToolbar
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.adapter.ResumoPedidoProdutoVisualAdapter
import com.example.apkstelladitalia20.bottomsheet.BottomSheetFormaPagamento
import com.example.apkstelladitalia20.controller.CarrinhoController
import com.example.apkstelladitalia20.repository.PedidoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import java.util.Date
import java.util.Locale

class ResumoPedidoProdutoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResumoPedidoBinding
    private var listaCarrinho: ArrayList<ProdutoEntity> = arrayListOf()
    private var resumoAdapter = ResumoPedidoProdutoVisualAdapter(listaCarrinho)

    private var subtotal = 0.0
    private var taxaEntrega = 0.0
    private var descontoCupom = 0.0
    private var cpfNaNota: String? = null
    private var formaPagamentoSelecionada: String? = null
    private var trocoPara: String? = null
    private lateinit var prefs: SharedPreferences
    private val REQUEST_CODE_MERCADO_PAGO = 1234
    private var pedidoParaSalvar: PedidoEntity? = null
    private var enderecoSelecionado: EnderecoEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResumoPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("appStella", Context.MODE_PRIVATE)
        setupToolbar(binding.includeToolbar)
        setupListeners()

        listaCarrinho =
            intent.getSerializableExtra("carrinhoSelecionado") as? ArrayList<ProdutoEntity>
                ?: arrayListOf()

        setupRecyclerResumo()
        carregarTaxaEntregaFirebase()
        atualizarResumoValores()

    }


    private fun setupListeners() {
        binding.btnConfirmarPedido.setOnClickListener {
            val pedido = PedidoEntity(
                numero = gerarNumeroAleatorio(),
                nomeLoja = "Stella D’Italia - Maringá",
                dataHora = obterDataHoraAtual(),
                status = "aguardando",
                itens = CarrinhoController.getItens(),
                subtotal = calcularSubtotal(),
                desconto = calcularDesconto(),
                entrega = "Grátis",
                total = calcularTotalFinal(),
                formaPagamento = formaPagamentoSelecionada ?: "Não informado",
                enderecoEntrega = enderecoSelecionado.toString(),
                clienteId = FirebaseAuth.getInstance().uid ?: "",
                nomeCliente = FirebaseAuth.getInstance().currentUser?.displayName ?: "Anônimo",
                telefoneCliente = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""
            )

// Salva o ID do pedido temporariamente
            val pedidoId = FirebaseDatabase.getInstance()
                .getReference("empresa")
                .child(prefs.getString("uidEmpresa", "") ?: "")
                .child("pedidos")
                .push().key ?: return@setOnClickListener


// Salva temporariamente no Firebase para uso posterior (ou pode guardar local)
            FirebaseDatabase.getInstance().reference
                .child("tempPedidos")
                .child(pedidoId)
                .setValue(pedido)


            iniciarPagamentoStripe(
                valorTotal = pedido.total,
                nomeCliente = pedido.nomeCliente,
                idEmpresa = prefs.getString("uidEmpresa", "") ?: "",
                pedidoId = pedidoId
            )
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_MERCADO_PAGO) {
            if (resultCode == RESULT_OK) {
                val paymentId = data?.getLongExtra("payment_id", -1L) ?: -1L
                val paymentStatus = data?.getStringExtra("payment_status")

                if (paymentStatus == "approved") {
                    pedidoParaSalvar?.let { pedido ->
                        PedidoRepository.salvarPedido(
                            this, pedido,
                            onSuccess = {
                                // Salva também no histórico do cliente
                                PedidoRepository.salvarPedidoDoCliente(pedido)

                                Toast.makeText(
                                    this,
                                    "Pedido salvo com sucesso!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            },
                            onError = { erro ->
                                Toast.makeText(
                                    this,
                                    "Erro ao salvar pedido: ${erro.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                } else {
                    Toast.makeText(this, "Pagamento falhou: $paymentStatus", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(this, "Pagamento cancelado.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun salvarPedidoNoFirebase(pedido: PedidoEntity) {
        val uidEmpresa = prefs.getString("uidEmpresa", null) ?: return
        val pedidoRef = FirebaseDatabase.getInstance()
            .getReference("empresa")
            .child(uidEmpresa)
            .child("pedidos")

        val novoId = pedidoRef.push().key ?: return

        pedidoRef.child(novoId).setValue(pedido)
            .addOnSuccessListener {
                Toast.makeText(this, "Pedido enviado com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar pedido", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRecyclerResumo() {
        resumoAdapter = ResumoPedidoProdutoVisualAdapter(listaCarrinho)
        binding.recyclerResumoPedido.layoutManager = LinearLayoutManager(this)
        binding.recyclerResumoPedido.adapter = resumoAdapter
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

    private fun atualizarResumoValores() {
        subtotal = listaCarrinho.sumOf { it.getPrecoReal() * it.quantidade }
        val subtotalComDesconto = (subtotal - descontoCupom).coerceAtLeast(0.0)
        val total = subtotalComDesconto + taxaEntrega

        binding.txtSubtotalResumo.text = "R$ %.2f".format(subtotalComDesconto)
        binding.txtTaxaEntregaResumo.text =
            if (taxaEntrega == 0.0) "Grátis" else "R$ %.2f".format(taxaEntrega)
        binding.txtTotalResumo.text = "R$ %.2f".format(total)
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

    private fun gerarNumeroAleatorio(): Int {
        return (1000..9999).random()
    }

    private fun obterDataHoraAtual(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun calcularSubtotal(): Double {
        return CarrinhoController.getItens().map { it.precoAtual * it.quantidade }.sum()

    }

    private fun calcularDesconto(): Double {
        return 0.0
    }

    private fun calcularTotalFinal(): Double {
        return calcularSubtotal() - calcularDesconto()
    }
    private fun iniciarPagamentoStripe(
        valorTotal: Double,
        nomeCliente: String,
        idEmpresa: String,
        pedidoId: String
    ) {
        val functions = Firebase.functions

        val dados = hashMapOf(
            "valorEmCentavos" to (valorTotal * 100).toInt(),
            "nomeCliente" to nomeCliente,
            "idEmpresa" to idEmpresa
        )

        functions
            .getHttpsCallable("criarPagamentoStripe")
            .call(dados)
            .addOnSuccessListener { result ->
                val url = (result.data as Map<*, *>)["url"] as? String ?: return@addOnSuccessListener

                val intent = Intent(this, WebViewPagamentoActivity::class.java)
                intent.putExtra("url", url)
                intent.putExtra("pedidoId", pedidoId)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun abrirCheckoutWebView(url: String) {
        val intent = Intent(this, WebViewPagamentoActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }

}

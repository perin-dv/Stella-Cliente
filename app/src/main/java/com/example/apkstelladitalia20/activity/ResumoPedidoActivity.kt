package com.example.apkstelladitalia20.activity


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.databinding.ActivityResumoPedidoBinding
import com.example.apkstelladitalia20.helper.setupToolbar
import com.example.apkstelladitalia20.adapter.ResumoPedidoProdutoVisualAdapter
import com.example.apkstelladitalia20.bottomsheet.BottomSheetFormaPagamento
import com.example.apkstelladitalia20.dialog.PagamentoDinheiroDialogFragment
import com.example.apkstelladitalia20.model.CarrinhoViewModel
import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity
import com.example.apkstelladitalia20.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase


import java.util.Date
import java.util.Locale
import java.util.UUID

class ResumoPedidoProdutoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResumoPedidoBinding
    private lateinit var viewModel: CarrinhoViewModel
    private val listaCarrinho = mutableListOf<ProdutoCarrinhoEntity>()
    private lateinit var adapter: ResumoPedidoProdutoVisualAdapter
    private var pedido: PedidoEntity? = null


    private var subtotal = 0.0
    private var taxaEntrega = 0.0
    private var descontoCupom = 0.0
    private var cpfNaNota: String? = null
    private var formaPagamentoSelecionada: String? = null
    private var trocoPara: String? = null
    private lateinit var prefs: SharedPreferences
    private var tipoPagamentoSelecionado: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResumoPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.includeToolbar)

        prefs = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[CarrinhoViewModel::class.java]
        val prefs = getSharedPreferences("appStella", MODE_PRIVATE)
        val uid = prefs.getString("uidCliente", "NULO")
        Log.d("DEBUG_UID", "Cliente UID: $uid")




        setupRecyclerResumo()
        setupListeners()
        carregarTaxaEntregaFirebase()
        mostrarBottomSheetPagamento()

        binding.txtFormaPagamento.text = "Selecione a forma de pagamento"

        pedido = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("pedidoTemp", PedidoEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("pedidoTemp")
        }

        if (pedido == null || pedido!!.itens.isNullOrEmpty()) {
            Toast.makeText(this, "Pedido inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }



        viewModel.itensCarrinho.observe(this) { lista ->
            listaCarrinho.clear()
            listaCarrinho.addAll(lista)
            adapter.notifyDataSetChanged()
            atualizarResumoValores()
        }
        Log.d("PEDIDO_DEBUG", "Recebido no ResumoPedido: $pedido")
    }

    private fun setupRecyclerResumo() {
        adapter = ResumoPedidoProdutoVisualAdapter(listaCarrinho)
        binding.recyclerResumoPedido.layoutManager = LinearLayoutManager(this)
        binding.recyclerResumoPedido.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnConfirmarPedido.setOnClickListener {
            if (formaPagamentoSelecionada.isNullOrEmpty()) {
                Toast.makeText(this, "Selecione uma forma de pagamento", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            executarFluxoConfirmacaoDePedido()
        }


        binding.txtTrocarPagamento.setOnClickListener {
            BottomSheetFormaPagamento(
                onPagamentoSelecionado = { forma, troco,tipo ->
                    formaPagamentoSelecionada = forma
                    trocoPara = troco
                    tipoPagamentoSelecionado = tipo
                },
                pagamentoCallback = { forma, textoVisivel ->
                    val texto = when (forma) {
                        "Dinheiro" -> "Dinheiro"
                        else -> forma
                    }
                    binding.txtFormaPagamento.text = texto
                }

            ).show(supportFragmentManager, "BottomSheetFormaPagamento")
        }






        binding.txtAdicionarCupom.setOnClickListener {
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT

            AlertDialog.Builder(this)
                .setTitle("Adicionar Cupom")
                .setView(input)
                .setPositiveButton("Aplicar") { _, _ ->
                    val cupom = input.text.toString()
                    if (cupom.equals("DESCONTO10", true)) {
                        binding.txtCupomAplicado.text = "Cupom aplicado: $cupom"
                        binding.txtCupomAplicado.visibility = View.VISIBLE
                        Toast.makeText(this, "✅ Cupom aplicado!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "❌ Cupom inválido.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        binding.txtCpfNota.setOnClickListener {
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_NUMBER

            AlertDialog.Builder(this)
                .setTitle("CPF na Nota")
                .setView(input)
                .setPositiveButton("Salvar") { _, _ ->
                    val cpf = input.text.toString()
                    if (cpf.length == 11) {
                        val formatado =
                            cpf.replace(Regex("(\\d{3})(\\d{3})(\\d{3})(\\d{2})"), "$1.$2.$3-$4")
                        binding.txtCpfAplicado.text = "CPF na nota: $formatado"
                        binding.txtCpfAplicado.visibility = View.VISIBLE
                        Toast.makeText(this, "✅ CPF adicionado!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "❌ CPF inválido!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun solicitarBiometria(onSuccess: () -> Unit) {
        val biometricManager = androidx.biometric.BiometricManager.from(this)
        val executor = androidx.core.content.ContextCompat.getMainExecutor(this)

        when (biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> {
                val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Confirme sua identidade")
                    .setSubtitle("Use biometria para autorizar esta compra")
                    .setNegativeButtonText("Cancelar")
                    .build()

                val biometricPrompt = androidx.biometric.BiometricPrompt(
                    this, executor,
                    object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            onSuccess()
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            Toast.makeText(
                                this@ResumoPedidoProdutoActivity,
                                "Biometria cancelada",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            Toast.makeText(
                                this@ResumoPedidoProdutoActivity,
                                "Biometria não reconhecida",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })

                biometricPrompt.authenticate(promptInfo)
            }

            else -> {
                Toast.makeText(this, "Biometria não disponível no dispositivo", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun carregarTaxaEntregaFirebase() {
        val ref = FirebaseDatabase.getInstance()
            .getReference("empresa")
            .child(Constants.UID_EMPRESA_FIXO)
            .child("taxadefrete")

        ref.get().addOnSuccessListener {
            taxaEntrega = it.getValue(Double::class.java) ?: 0.0
            atualizarResumoValores()
        }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar taxa de entrega.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun atualizarResumoValores() {
        subtotal = listaCarrinho.sumOf { it.valor * it.quantidade }
        val total = subtotal + taxaEntrega - descontoCupom

        binding.txtSubtotalResumo.text = "R$ %.2f".format(subtotal)
        binding.txtTaxaEntregaResumo.text =
            if (taxaEntrega > 0) "R$ %.2f".format(taxaEntrega) else "Grátis"
        binding.txtTotalResumo.text = "R$ %.2f".format(total)
    }

    private fun aplicarCupom() {
        val cupomDigitado = binding.txtAdicionarCupom.text.toString().trim()
        if (cupomDigitado.equals("DESCONTO10", ignoreCase = true)) {
            descontoCupom = 10.0
            Toast.makeText(this, "Cupom aplicado com sucesso!", Toast.LENGTH_SHORT).show()
        } else if (cupomDigitado.equals("FRETEGRATIS", ignoreCase = true)) {
            taxaEntrega = 0.0
            Toast.makeText(this, "Frete gratuito aplicado!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Cupom inválido.", Toast.LENGTH_SHORT).show()
        }
        atualizarResumoValores()
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

    private fun executarFluxoConfirmacaoDePedido() {
        val uidEmpresa = Constants.UID_EMPRESA_FIXO
        val clienteId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val novoId = UUID.randomUUID().toString()

        FirebaseDatabase.getInstance()
            .getReference("clientes")
            .child(clienteId)
            .get()
            .addOnSuccessListener { snapshot ->
                val nome = snapshot.child("nome").value?.toString() ?: ""
                val telefone = snapshot.child("telefone").value?.toString() ?: ""
                val endereco = snapshot.child("endereco").value?.toString() ?: ""

                val pedido = PedidoEntity(
                    id = novoId,
                    clienteId = clienteId,
                    empresaId = uidEmpresa,
                    nomeCliente = nome,
                    telefoneCliente = telefone,
                    email = FirebaseAuth.getInstance().currentUser?.email ?: "",
                    enderecoEntrega = endereco,
                    itens = listaCarrinho.toList(),
                    subtotal = calcularSubtotal(),
                    desconto = calcularDesconto(),
                    entrega = if (taxaEntrega == 0.0) "Grátis" else "R$ %.2f".format(taxaEntrega),
                    total = calcularTotalFinal(),
                    formaPagamento = formaPagamentoSelecionada ?: "Não informado",
                    status = "pendente",
                    dataHora = obterDataHoraAtual(),
                    observacao = binding.edtObservacao.text.toString(),
                    cpfNota = cpfNaNota ?: ""
                )

                when (formaPagamentoSelecionada) {
                    "Pix" -> {
                        val data = hashMapOf(
                            "idEmpresa" to uidEmpresa,
                            "nomeCliente" to pedido.nomeCliente,
                            "telefoneCliente" to pedido.telefoneCliente,
                            "enderecoEntrega" to pedido.enderecoEntrega,
                            "valorTotal" to pedido.total, // ✔️ Mantido como Double
                            "pedidoId" to pedido.id
                        )

                        try {
                            val functions = FirebaseFunctions.getInstance("us-central1")
                            functions
                                .getHttpsCallable("criarPagamentoPix")
                                .call(data)
                                .addOnSuccessListener { result ->
                                    val map = result.data as Map<*, *>
                                    val qrBase64 = map["qr_base64"] as? String
                                    val qrString = map["qr_string"] as? String

                                    val encodedQrBase64 = Uri.encode(qrBase64 ?: "")
                                    val encodedQrString = Uri.encode(qrString ?: "")

                                    val url = "https://stella-d-italia.web.app/pagamento_pix.html" +
                                            "?qr_base64=$encodedQrBase64&qr_string=$encodedQrString"

                                    val intent =
                                        Intent(this, PagamentoCartaoWebActivity::class.java)
                                    intent.putExtra("url_pagamento", url)
                                    intent.putExtra("pedidoTemp", pedido as Parcelable)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Log.e("PIX_ERROR", "Erro: ${it.message}")
                                    Toast.makeText(
                                        this,
                                        "Erro ao gerar Pix: ${it.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } catch (e: Exception) {
                            Toast.makeText(this, "Erro interno: ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                            Log.e("PIX_CATCH", "Falha geral: ${e.message}")
                        }

                    }

                    "Cartão de Crédito", "Cartão de Débito" -> {
                        if (tipoPagamentoSelecionado == "Entrega") {
                            val formaFinal = when (formaPagamentoSelecionada) {
                                "Cartão de Crédito" -> "Cartão de Crédito (entrega)"
                                "Cartão de Débito" -> "Cartão de Débito (entrega)"
                                else -> formaPagamentoSelecionada ?: "Entrega"
                            }

                            salvarPedidoFirebase(
                                pedido.copy(
                                    status = "confirmado",
                                    formaPagamento = formaFinal
                                )
                            )
                        }
                        else {
                            // PAGAMENTO PELO APP – SEGUE COM WEBVIEW
                            val functions = Firebase.functions("us-central1")

                            val data = mapOf(
                                "idEmpresa" to pedido.empresaId,
                                "nomeCliente" to pedido.nomeCliente,
                                "telefoneCliente" to pedido.telefoneCliente,
                                "enderecoEntrega" to pedido.enderecoEntrega,
                                "valorTotal" to pedido.total.toString(),
                                "pedidoId" to pedido.id,
                                "email" to pedido.email
                            )

                            functions
                                .getHttpsCallable("gerarCheckoutPagamento")
                                .call(data)
                                .addOnSuccessListener { result ->
                                    val preferenceId = (result.data as Map<*, *>)["id"] as? String

                                    if (preferenceId.isNullOrEmpty()) {
                                        Toast.makeText(this, "Erro: preferenceId não encontrado", Toast.LENGTH_SHORT).show()
                                        return@addOnSuccessListener
                                    }

                                    val url = "https://stella-d-italia.web.app/pagamento_cartao.html" +
                                            "?preference_id=$preferenceId" +
                                            "&valor=${pedido.total}" +
                                            "&email=${pedido.email}" +
                                            "&cpf=${pedido.cpfNota}" +
                                            "&empresaId=${pedido.empresaId}" +
                                            "&clienteId=${pedido.clienteId}" +
                                            "&pedidoId=${pedido.id}"
                                    Log.d("URL_PAGAMENTO", url)

                                    val ref = FirebaseDatabase.getInstance()
                                        .getReference("pedidos_temporarios")
                                        .child(pedido.clienteId)
                                        .child(pedido.id)

                                    ref.setValue(pedido)


                                    val intent = Intent(this, PagamentoCartaoWebActivity::class.java)
                                    intent.putExtra("url_pagamento", url)
                                    intent.putExtra("pedidoTemp", pedido as Parcelable)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Erro ao gerar pagamento: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    }




                    "Dinheiro" -> {
                        PagamentoDinheiroDialogFragment(pedido) { pedidoFinal ->
                            salvarPedidoFirebase(pedidoFinal.copy(status = "confirmado"))
                        }.show(supportFragmentManager, "pagamentoDinheiro")
                    }

                    else -> {
                        Toast.makeText(this, "Escolha uma forma de pagamento", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao buscar dados do cliente!", Toast.LENGTH_SHORT).show()
            }
    }


    private fun salvarPedidoFirebase(pedido: PedidoEntity) {
        val empresaRef = FirebaseDatabase.getInstance()
            .getReference("pedidos_confirmados")
            .child(pedido.empresaId)
            .child(pedido.id)

        val clienteRef = FirebaseDatabase.getInstance()
            .getReference("clientes")
            .child(pedido.clienteId)
            .child("pedidos")
            .child(pedido.id)

        empresaRef.setValue(pedido)
        clienteRef.setValue(pedido).addOnSuccessListener {


            viewModel.limparCarrinho()
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("abrir_pedidos", true)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()

        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao salvar pedido!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarBottomSheetPagamento() {
        val bottomSheet = BottomSheetFormaPagamento(
            onPagamentoSelecionado = { forma, troco,tipo  ->
                formaPagamentoSelecionada = forma
                trocoPara = troco
                tipoPagamentoSelecionado = tipo
            },
            pagamentoCallback = { forma, _ ->
                binding.txtFormaPagamento.text = when (forma) {
                    "Dinheiro" -> "💵 Dinheiro"
                    "Pix" -> "⚡ Pix"
                    "Cartão de Crédito" -> "💳 Cartão de Crédito"
                    "Cartão de Débito" -> "🏧 Cartão de Débito"
                    else -> "Forma de pagamento"
                }
            }
        )
        bottomSheet.show(supportFragmentManager, "BottomSheetFormaPagamento")
    }



    private fun obterDataHoraAtual(): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date())
        }

        private fun calcularSubtotal(): Double {
            return listaCarrinho.sumOf { it.valor * it.quantidade }
        }

        private fun calcularDesconto(): Double {
            return descontoCupom
        }

        private fun calcularTotalFinal(): Double {
            return calcularSubtotal() + taxaEntrega - calcularDesconto()
        }



    }

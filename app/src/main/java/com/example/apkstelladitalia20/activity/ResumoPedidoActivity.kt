package com.example.apkstelladitalia20.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.Entity.EnderecoEntity
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.databinding.ActivityResumoPedidoBinding
import com.example.apkstelladitalia20.helper.setupToolbar
import com.example.apkstelladitalia20.adapter.ResumoPedidoProdutoVisualAdapter
import com.example.apkstelladitalia20.bottomsheet.BottomSheetFormaPagamento
import com.example.apkstelladitalia20.model.CarrinhoViewModel
import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity
import com.example.apkstelladitalia20.repository.PedidoRepository
import com.example.apkstelladitalia20.ui.dialog.ConfirmacaoPedidoDialogFragment
import com.example.apkstelladitalia20.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
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

        viewModel.itensCarrinho.observe(this) { lista ->
            listaCarrinho.clear()
            listaCarrinho.addAll(lista)
            adapter.notifyDataSetChanged()
            atualizarResumoValores()
        }

    }

    private fun setupRecyclerResumo() {
        adapter = ResumoPedidoProdutoVisualAdapter(listaCarrinho)
        binding.recyclerResumoPedido.layoutManager = LinearLayoutManager(this)
        binding.recyclerResumoPedido.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnConfirmarPedido.setOnClickListener {
                if (formaPagamentoSelecionada.isNullOrEmpty()) {
                    Toast.makeText(this, "Escolha uma forma de pagamento!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // ✅ Verificar se biometria está ativada
                val prefs = getSharedPreferences("appStella", MODE_PRIVATE)
                val biometriaHabilitada = prefs.getBoolean("biometria_habilitada", false)

                if (biometriaHabilitada) {
                    solicitarBiometria {
                        executarFluxoConfirmacaoDePedido()
                    }
                } else {
                    executarFluxoConfirmacaoDePedido()
                }



        val uidEmpresa = Constants.UID_EMPRESA_FIXO
            val novoId = UUID.randomUUID().toString()
            val clienteId = getSharedPreferences("appStella", MODE_PRIVATE)
                .getString("uidCliente", null)

            if (clienteId.isNullOrEmpty()) {
                Toast.makeText(this, "Erro: cliente não logado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // Buscar endereço do cliente
            FirebaseDatabase.getInstance()
                .getReference("clientes")
                .child(clienteId)
                .get()
                .addOnSuccessListener { snapshot ->
                    FirebaseDatabase.getInstance()
                        .getReference("clientes")
                        .child(clienteId)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val endereco =
                                snapshot.child("endereco").value?.toString() ?: "Não informado"
                            val nome = snapshot.child("nome").value?.toString() ?: "Cliente"
                            val telefone = snapshot.child("telefone").value?.toString() ?: ""

                            val pedido = PedidoEntity(
                                id = novoId,
                                numero = "",
                                nomeLoja = "Stella D’Italia – Maringá",
                                dataHora = obterDataHoraAtual(),
                                status = "aguardando",
                                itens = listaCarrinho.toList(),
                                subtotal = calcularSubtotal(),
                                desconto = calcularDesconto(),
                                entrega = if (taxaEntrega == 0.0) "Grátis" else "R$ %.2f".format(
                                    taxaEntrega
                                ),
                                total = calcularTotalFinal(),
                                formaPagamento = formaPagamentoSelecionada ?: "Não informado",
                                enderecoEntrega = endereco,
                                clienteId = clienteId,
                                nomeCliente = nome,
                                telefoneCliente = telefone,
                                observacao = binding.edtObservacao.text.toString()
                            )

                            pedidoParaSalvar = pedido

                            if (formaPagamentoSelecionada.equals("Pix", ignoreCase = true)) {
                                val data = hashMapOf(
                                    "idEmpresa" to uidEmpresa,
                                    "nomeCliente" to nome,
                                    "telefoneCliente" to telefone,
                                    "enderecoEntrega" to endereco,
                                    "valorTotal" to String.format(Locale.US, "%.2f", pedido.total),
                                    "pedidoId" to pedido.id,
                                    "observacao" to pedido.observacao
                                )

                                FirebaseFunctions.getInstance()
                                    .getHttpsCallable("criarPagamentoPix")
                                    .call(data)
                                    .addOnSuccessListener { result ->
                                        val map = result.data as Map<*, *>
                                        val qrBase64 = map["qr_base64"] as? String
                                        val qrString = map["qr_string"] as? String

                                        val intent = Intent(this, PagamentoPixActivity::class.java)
                                        intent.putExtra("qr_base64", qrBase64)
                                        intent.putExtra("qr_string", qrString)
                                        intent.putExtra("pedidoTemp", pedido)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Log.e("PIX_ERROR", "Erro ao gerar pagamento Pix", it)
                                        Toast.makeText(
                                            this,
                                            "Erro ao gerar pagamento Pix",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                            } else {
                                ConfirmacaoPedidoDialogFragment(pedido) { pedidoConfirmado ->
                                    val pedidoFinal = pedidoConfirmado.copy(status = "confirmado")
                                    val ref = FirebaseDatabase.getInstance()
                                        .getReference("pedidos_confirmados")
                                        .child(Constants.UID_EMPRESA_FIXO)
                                        .child(pedidoFinal.id)

                                    ref.setValue(pedidoFinal)
                                        .addOnSuccessListener {
                                            val intent = Intent(this, HomeActivity::class.java)
                                            intent.putExtra("abrir_pedidos", true)
                                            intent.flags =
                                                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                            startActivity(intent)
                                            finish()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                this,
                                                "❌ Erro ao salvar pedido",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }.show(supportFragmentManager, "confirmacaoDialog")
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Erro ao buscar dados do cliente!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
        }



        binding.txtTrocarPagamento.setOnClickListener {
            mostrarBottomSheetPagamento()
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

                val biometricPrompt = androidx.biometric.BiometricPrompt(this, executor,
                    object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            onSuccess()
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            Toast.makeText(this@ResumoPedidoProdutoActivity, "Biometria cancelada", Toast.LENGTH_SHORT).show()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            Toast.makeText(this@ResumoPedidoProdutoActivity, "Biometria não reconhecida", Toast.LENGTH_SHORT).show()
                        }
                    })

                biometricPrompt.authenticate(promptInfo)
            }

            else -> {
                Toast.makeText(this, "Biometria não disponível no dispositivo", Toast.LENGTH_SHORT).show()
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
        val novoId = UUID.randomUUID().toString()
        val clienteId = getSharedPreferences("appStella", MODE_PRIVATE)
            .getString("uidCliente", null)

        if (clienteId.isNullOrEmpty()) {
            Toast.makeText(this, "Erro: cliente não logado", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseDatabase.getInstance()
            .getReference("clientes")
            .child(clienteId)
            .get()
            .addOnSuccessListener { snapshot ->
                val endereco = snapshot.child("endereco").value?.toString() ?: "Não informado"
                val nome = snapshot.child("nome").value?.toString() ?: "Cliente"
                val telefone = snapshot.child("telefone").value?.toString() ?: ""

                val pedido = PedidoEntity(
                    id = novoId,
                    numero = "",
                    nomeLoja = "Stella D’Italia – Maringá",
                    dataHora = obterDataHoraAtual(),
                    status = "aguardando",
                    itens = listaCarrinho.toList(),
                    subtotal = calcularSubtotal(),
                    desconto = calcularDesconto(),
                    entrega = if (taxaEntrega == 0.0) "Grátis" else "R$ %.2f".format(taxaEntrega),
                    total = calcularTotalFinal(),
                    formaPagamento = formaPagamentoSelecionada ?: "Não informado",
                    enderecoEntrega = endereco,
                    clienteId = clienteId,
                    nomeCliente = nome,
                    telefoneCliente = telefone,
                    observacao = binding.edtObservacao.text.toString()
                )

                pedidoParaSalvar = pedido

                if (formaPagamentoSelecionada.equals("Pix", ignoreCase = true)) {
                    val data = hashMapOf(
                        "idEmpresa" to uidEmpresa,
                        "nomeCliente" to nome,
                        "telefoneCliente" to telefone,
                        "enderecoEntrega" to endereco,
                        "valorTotal" to String.format(Locale.US, "%.2f", pedido.total),
                        "pedidoId" to pedido.id,
                        "observacao" to pedido.observacao
                    )

                    FirebaseFunctions.getInstance()
                        .getHttpsCallable("criarPagamentoPix")
                        .call(data)
                        .addOnSuccessListener { result ->
                            val map = result.data as Map<*, *>
                            val qrBase64 = map["qr_base64"] as? String
                            val qrString = map["qr_string"] as? String

                            val intent = Intent(this, PagamentoPixActivity::class.java)
                            intent.putExtra("qr_base64", qrBase64)
                            intent.putExtra("qr_string", qrString)
                            intent.putExtra("pedidoTemp", pedido)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Log.e("PIX_ERROR", "Erro ao gerar pagamento Pix", it)
                            Toast.makeText(
                                this,
                                "Erro ao gerar pagamento Pix",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                } else {
                    ConfirmacaoPedidoDialogFragment(pedido) { pedidoConfirmado ->
                        val pedidoFinal = pedidoConfirmado.copy(status = "confirmado")
                        val ref = FirebaseDatabase.getInstance()
                            .getReference("pedidos_confirmados")
                            .child(Constants.UID_EMPRESA_FIXO)
                            .child(pedidoFinal.id)

                        ref.setValue(pedidoFinal)
                            .addOnSuccessListener {
                                val intent = Intent(this, HomeActivity::class.java)
                                intent.putExtra("abrir_pedidos", true)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "❌ Erro ao salvar pedido",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }.show(supportFragmentManager, "confirmacaoDialog")
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Erro ao buscar dados do cliente!",
                    Toast.LENGTH_SHORT
                ).show()
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

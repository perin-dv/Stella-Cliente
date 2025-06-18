package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.databinding.ActivityPagamentoCartaoWebBinding
import com.example.apkstelladitalia20.model.CarrinhoViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions

class PagamentoCartaoWebActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPagamentoCartaoWebBinding
    private lateinit var pedido: PedidoEntity
    private lateinit var viewModel: CarrinhoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPagamentoCartaoWebBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.toolbarPagamento)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarPagamento.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // ViewModel
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[CarrinhoViewModel::class.java]

        // Recupera pedido
        pedido = intent.getParcelableExtra("pedidoTemp") ?: run {
            Toast.makeText(this, "Pedido inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Verifica URL
        val url = intent.getStringExtra("url_pagamento")
        if (url.isNullOrEmpty()) {
            Toast.makeText(this, "URL inválida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Configura WebView
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
            addJavascriptInterface(WebAppInterface(), "AndroidInterface")
            loadUrl(url)
        }
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun retornarStatusPagamento(status: String, statusDetail: String) {
            runOnUiThread {
                if (status == "approved") {
                    salvarPedidoConfirmado()
                    viewModel.limparCarrinho()
                } else {
                    val msg = traduzirErroStatus(statusDetail)
                    Toast.makeText(this@PagamentoCartaoWebActivity, "❌ $msg", Toast.LENGTH_LONG).show()
                }
            }
        }
    }



    private fun traduzirErroStatus(statusDetail: String?): String {
        return when (statusDetail) {
            "cc_rejected_insufficient_amount" -> "Recusado: saldo insuficiente."
            "cc_rejected_bad_filled_security_code" -> "Código de segurança inválido."
            "cc_rejected_bad_filled_date" -> "Data de validade incorreta."
            "cc_rejected_bad_filled_card_number" -> "Número do cartão inválido."
            "cc_rejected_other_reason" -> "Recusado: erro geral no pagamento."
            "cc_rejected_call_for_authorize" -> "Recusado: autorize com o banco."
            "pending_waiting_transfer" -> "Aguardando transferência do banco."
            else -> "Pagamento recusado ou erro desconhecido."
        }
    }

    private fun salvarPedidoConfirmado() {
        val pedidoConfirmado = pedido.copy(status = "confirmado")

        val empresaRef = FirebaseDatabase.getInstance()
            .getReference("pedidos_confirmados")
            .child(pedido.empresaId)
            .child(pedido.id)

        val clienteRef = FirebaseDatabase.getInstance()
            .getReference("clientes")
            .child(pedido.clienteId)
            .child("pedidos")
            .child(pedido.id)

        empresaRef.setValue(pedidoConfirmado)
        clienteRef.setValue(pedidoConfirmado).addOnSuccessListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("abrirAba", "pedidos")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao salvar pedido!", Toast.LENGTH_SHORT).show()
        }
    }
}

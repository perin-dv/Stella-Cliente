@file:Suppress("DEPRECATION")

package com.example.apkstelladitalia20.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.model.CarrinhoViewModel
import com.example.apkstelladitalia20.fragment.PedidosFragment
import com.example.apkstelladitalia20.util.Constants
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import java.util.Locale

class PagamentoPixActivity : AppCompatActivity() {
    private var pedido: PedidoEntity? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagamento_pix)

        pedido = intent.getParcelableExtra("pedidoTemp")


        val qrBase64 = intent.getStringExtra("qr_base64")
        val qrString = intent.getStringExtra("qr_string")

        val imgQrCode = findViewById<ImageView>(R.id.imgQrCode)
        val txtPixCopiaCola = findViewById<TextView>(R.id.txtPixCopiaCola)
        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltarPix)


        val btnCopiar = findViewById<Button>(R.id.btnCopiarPix)
        btnCopiar.setOnClickListener {
            qrString?.let {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("PIX", it)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Código Pix copiado!", Toast.LENGTH_SHORT).show()
            }
        }

        qrBase64?.let {
            val imageBytes = Base64.decode(it, Base64.DEFAULT)
            val qrBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imgQrCode.setImageBitmap(qrBitmap)
        }

        txtPixCopiaCola.text = qrString ?: "Pix indisponível"
        btnVoltar.setOnClickListener {
            val intent = Intent(this, ResumoPedidoProdutoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        val btnFakeConfirmar = findViewById<Button>(R.id.btnFakeConfirmar)

        btnFakeConfirmar.setOnClickListener {
            val pedidoConfirmado = pedido ?: run {
                Log.e("PagamentoPix", "❌ Pedido é nulo!")
                Toast.makeText(this, "Erro: Pedido não encontrado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val clienteId = pedidoConfirmado.clienteId
            if (clienteId.isNullOrBlank()) {
                Toast.makeText(this, "Erro: clienteId ausente no pedido!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val empresaId = Constants.UID_EMPRESA_FIXO
            val pedidoId = pedidoConfirmado.id
            val pedidoFinal = pedidoConfirmado.copy(status = "aguardando_confirmacao")

            val refEmpresa = FirebaseDatabase.getInstance()
                .getReference("pedidos_confirmados")
                .child(empresaId)
                .child(pedidoId)

            val refCliente = FirebaseDatabase.getInstance()
                .getReference("clientes")
                .child(clienteId)
                .child("pedidos")
                .child(pedidoId)

            // Salva na empresa
            refEmpresa.setValue(pedidoFinal)
                .addOnSuccessListener {
                    // Salva no cliente
                    refCliente.setValue(pedidoFinal)
                        .addOnSuccessListener {
                            val data = hashMapOf(
                                "idEmpresa" to empresaId,
                                "nomeCliente" to pedidoFinal.nomeCliente,
                                "telefoneCliente" to pedidoFinal.telefoneCliente,
                                "enderecoEntrega" to pedidoFinal.enderecoEntrega,
                                "valorTotal" to String.format(Locale.US, "%.2f", pedidoFinal.total),
                                "pedidoId" to pedidoId,
                                "observacao" to pedidoFinal.observacao
                            )

                            Firebase.functions.getHttpsCallable("criarPagamentoPix")
                                .call(data)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Pix efetuado com sucesso!!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "❌ Erro ao gerar pagamento Pix", Toast.LENGTH_SHORT).show()
                                }

                            Firebase.functions.getHttpsCallable("enviarNotificacaoPedido")
                                .call(data)
                                .addOnSuccessListener {
                                    Log.d("PagamentoPix", "✅ Pedido salvo e notificação enviada!")
                                    ViewModelProvider(this)[CarrinhoViewModel::class.java].limparCarrinho()

                                    // Limpa o carrinho corretamente
                                    ViewModelProvider(this)[CarrinhoViewModel::class.java].limparCarrinho()

// Volta para HomeActivity com aba "Pedidos" aberta
                                    val intent = Intent(this, HomeActivity::class.java).apply {
                                        putExtra("abrir_pedidos", true)
                                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    startActivity(intent)
                                    finishAffinity() // Garante que não volta para telas antigas

                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "⚠️ Pedido salvo, mas notificação falhou", Toast.LENGTH_SHORT).show()
                                    ViewModelProvider(this)[CarrinhoViewModel::class.java].limparCarrinho()

                                    val intent = Intent(this, HomeActivity::class.java)
                                    intent.putExtra("abrir_pedidos", true)
                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    startActivity(intent)
                                    finish()
                                }
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "❌ Erro ao salvar pedido", Toast.LENGTH_SHORT).show()
                }
        }


    }

}


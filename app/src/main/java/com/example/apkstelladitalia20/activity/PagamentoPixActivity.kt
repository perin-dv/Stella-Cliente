package com.example.apkstelladitalia20.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.ui.pedidos.PedidosFragment
import com.example.apkstelladitalia20.util.Constants
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase

class PagamentoPixActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagamento_pix)

        val qrBase64 = intent.getStringExtra("qr_base64")
        val qrString = intent.getStringExtra("qr_string")
        val pedido = intent.getSerializableExtra("pedidoTemp") as? PedidoEntity

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
            pedido?.let { pedidoConfirmado ->

                // 1. Salvar o pedido no Firebase com status aguardando
                val ref = FirebaseDatabase.getInstance()
                    .getReference("pedidos")
                    .child(Constants.UID_EMPRESA_FIXO)
                    .child(pedidoConfirmado.id)

                ref.setValue(pedidoConfirmado.copy(status = "aguardando"))
                    .addOnSuccessListener {

                        // 2. Disparar a função de notificação fake (opcional)
                        val functions = Firebase.functions
                        val paymentIdFake = pedidoConfirmado.id // pode usar o id real como fake

                        val data = hashMapOf(
                            "data" to mapOf("id" to paymentIdFake)
                        )

                        functions.getHttpsCallable("notificacaoPagamentoPix")
                            .call(data)
                            .addOnSuccessListener {
                                Toast.makeText(this, "✅ Simulação enviada!", Toast.LENGTH_SHORT)
                                    .show()

                                // 3. Ir para a tela final de confirmação
                                val intent = Intent(this, HomeActivity::class.java)
                                intent.putExtra("abrir_pedidos", true)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "⚠️ Pedido salvo, mas notificação falhou",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "❌ Erro ao salvar pedido simulado", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }


    }

}

package com.example.apkstelladitalia20.activity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class WebViewPagamentoActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var btnVoltar: ImageButton
    private lateinit var loading: ProgressBar
    private lateinit var imgSucesso: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview_pagamento)

        webView = findViewById(R.id.webViewStripe)
        btnVoltar = findViewById(R.id.btnVoltar)
        loading = findViewById(R.id.progressLoading)
        imgSucesso = findViewById(R.id.imgSucesso)

        val url = intent.getStringExtra("url") ?: return
        val pedidoId = intent.getStringExtra("pedidoId") ?: ""

        btnVoltar.setOnClickListener { finish() }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                loading.visibility = View.VISIBLE
                imgSucesso.visibility = View.GONE
            }

            override fun onPageFinished(view: WebView?, urlAtual: String?) {
                loading.visibility = View.GONE
                if (urlAtual?.contains("pagamento-sucesso") == true) {
                    mostrarSucesso()
                    salvarPedidoFirebase(pedidoId)
                }
            }
        }

        webView.loadUrl(url)
    }

    private fun mostrarSucesso() {
        imgSucesso.visibility = View.VISIBLE
        imgSucesso.alpha = 0f
        imgSucesso.animate().alpha(1f).setDuration(700).start()
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 2000)
    }

    private fun salvarPedidoFirebase(pedidoId: String) {
        val db = FirebaseDatabase.getInstance().reference
        val userId = FirebaseAuth.getInstance().uid ?: return
        db.child("usuarios").child(userId).child("pedidos").child(pedidoId)
            .child("status").setValue("confirmado")
    }
}

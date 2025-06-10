// AjudaActivity.kt
package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.R

class AjudaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajuda)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarAjuda)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val txtAjuda = findViewById<TextView>(R.id.txtAjuda)
        txtAjuda.text = "\uD83D\uDCC5 Sobre o aplicativo\n\n" +
                "Bem-vindo ao app oficial da Stella D’Italia – Maringá. Aqui você faz seus pedidos com rapidez, acompanha entregas em tempo real e aproveita promoções exclusivas.\n\n" +

                "\uD83D\uDCB3 Pagamentos\n\n" +
                "Aceitamos pagamentos via Pix, Cartão de Crédito e Dinheiro. Você poderá escolher sua forma preferida ao final do pedido.\n\n" +

                "\uD83D\uDCC6 Entregas\n\n" +
                "Nossos pedidos são entregues por motoboys parceiros com tempo médio de entrega entre 30 a 50 minutos. Você pode acompanhar seu pedido em tempo real pelo app.\n\n" +

                "\uD83D\uDD11 Segurança\n\n" +
                "Você pode habilitar o uso de biometria para confirmar seus pedidos. Basta ir até a aba \"Segurança\" no perfil.\n\n" +

                "\uD83D\uDCCA Histórico\n\n" +
                "Todos os seus pedidos ficam registrados. Você pode consultar ou repetir um pedido anterior pelo menu de \"Pedidos\".\n\n" +

                "\uD83D\uDCAC Suporte e ideias\n\n" +
                "Nos envie feedback sobre o aplicativo, ou compartilhe uma ideia. Vamos adorar ouvir sua opinião! \uD83D\uDE0A\n\n"

        val btnEnviar = findViewById<Button>(R.id.btnEnviarFeedback)
        val edtMensagem = findViewById<EditText>(R.id.edtMensagemAjuda)

        btnEnviar.setOnClickListener {
            val mensagem = edtMensagem.text.toString().trim()
            if (mensagem.isNotEmpty()) {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("perin_gui_@hotmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "Feedback Stella D’Italia")
                    putExtra(Intent.EXTRA_TEXT, mensagem)
                }
                startActivity(Intent.createChooser(emailIntent, "Enviar feedback com..."))
            } else {
                edtMensagem.error = "Digite sua sugestão ou feedback."
            }
        }
    }
}
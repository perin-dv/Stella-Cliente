package com.example.apkstelladitalia20.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.model.CardTokenRequest
import com.example.apkstelladitalia20.model.CardTokenResponse
import com.example.apkstelladitalia20.model.Cardholder
import com.example.apkstelladitalia20.service.MercadoPagoService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Callback

class NovoCartaoActivity : AppCompatActivity() {

    private lateinit var retrofit: Retrofit
    private lateinit var service: MercadoPagoService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novo_cartao)

        retrofit = Retrofit.Builder()
            .baseUrl("https://api.mercadopago.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(MercadoPagoService::class.java)

        findViewById<Button>(R.id.btnSalvarCartao).setOnClickListener {
            gerarToken()
        }
    }

    private fun gerarToken() {
        val cardNumber = findViewById<EditText>(R.id.editNumero).text.toString()
        val nome = findViewById<EditText>(R.id.editNome).text.toString()
        val validade = findViewById<EditText>(R.id.editValidade).text.toString().split("/")
        val cvv = findViewById<EditText>(R.id.editCVV).text.toString()

        if (validade.size < 2) {
            Toast.makeText(this, "Validade inválida", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CardTokenRequest(
            card_number = cardNumber,
            expiration_month = validade[0].toInt(),
            expiration_year = ("20" + validade[1]).toInt(),
            security_code = cvv,
            cardholder = Cardholder(nome)
        )

        service.criarToken(request).enqueue(object : Callback<CardTokenResponse> {
            override fun onResponse(call: Call<CardTokenResponse>, response: Response<CardTokenResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.id ?: ""
                    val last4 = response.body()?.last_four_digits ?: ""
                    salvarCartaoNoFirebase(token, last4, "desconhecida") // bandeira pode ser melhorada
                } else {
                    Toast.makeText(this@NovoCartaoActivity, "Erro ao gerar token", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CardTokenResponse>, t: Throwable) {
                Toast.makeText(this@NovoCartaoActivity, "Falha: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun salvarCartaoNoFirebase(token: String, last4: String, bandeira: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance()
            .getReference("clientes/$uid/cartoesSalvos")
            .push()

        val dados = mapOf(
            "token" to token,
            "ultimosDigitos" to last4,
            "bandeira" to bandeira,
            "criadoEm" to ServerValue.TIMESTAMP
        )

        ref.setValue(dados)
            .addOnSuccessListener {
                Toast.makeText(this, "Cartão salvo com segurança!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar", Toast.LENGTH_SHORT).show()
            }
    }
}

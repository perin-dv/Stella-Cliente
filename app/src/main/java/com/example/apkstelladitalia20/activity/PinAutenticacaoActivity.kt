package com.example.apkstelladitalia20.activity

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.helper.SegurancaUtils

class PinAutenticacaoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_autenticacao)

        val inputPin = findViewById<EditText>(R.id.editPin)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)

        btnConfirmar.setOnClickListener {
            val pinDigitado = inputPin.text.toString()
            val pinSalvo = SegurancaUtils.obterPIN(this)

            if (pinSalvo == null) {
                SegurancaUtils.salvarPIN(this, pinDigitado)
                Toast.makeText(this, "PIN salvo!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                if (pinSalvo == pinDigitado) {
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, "PIN incorreto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}


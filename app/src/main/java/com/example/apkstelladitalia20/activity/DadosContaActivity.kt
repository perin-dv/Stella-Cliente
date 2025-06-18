package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.databinding.ActivityDadosContaBinding

class DadosContaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDadosContaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDadosContaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.cardInfoPessoais.setOnClickListener {
            startActivity(Intent(this, InfoPessoaisActivity::class.java))
        }

        binding.cardInfoAcesso.setOnClickListener {
            startActivity(Intent(this, InfoAcessoActivity::class.java))
        }
    }
}
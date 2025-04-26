package com.example.apkstelladitalia20.activity

import android.os.Bundle
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.databinding.ActivityDetalhesPromocaoBinding
import com.example.apkstelladitalia20.model.Promocao

class PromocaoDetalhesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesPromocaoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesPromocaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val promocao = intent.getSerializableExtra("promocaoSelecionada") as? Promocao
        promocao?.let { exibirDetalhes(it) }
    }

    private fun exibirDetalhes(promocao: Promocao) {
        // Mostra imagem
        val bytes = Base64.decode(promocao.imagemBase64, Base64.DEFAULT)
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.imagemProduto.setImageBitmap(bmp)

        // Informações extras se quiser no layout
        binding.nomeProduto.text = promocao.titulo
        binding.descricaoProduto.text = promocao.observacao
        binding.precoProduto.text = "R$ %.2f".format(promocao.valor)

        // Pode adicionar aqui lógica para listar os produtos inclusos, etc
    }
}

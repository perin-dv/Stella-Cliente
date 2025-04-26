package com.example.apkstelladitalia20.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "produto")
data class ProdutoEntity(
    @PrimaryKey val id: String = "",
    val nome: String = "",
    val imagemBase64: String = "",
    val precoOriginal: Double = 0.0,
    val precoAtual: Double = 0.0,
    val maisPedido: Boolean = false,
    val quantidadeVendida: Int = 0,
    val categoria: String = "",
    val descricao: String = ""
)

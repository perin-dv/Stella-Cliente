package com.example.apkstelladitalia20.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carrinho")
data class ProdutoCarrinhoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    val idProduto: String,
    val nome: String,
    val valor: Double,
    val quantidade: Int = 1,

    val tipo: String = "normal", // "promocao" ou "normal"
    val descricao: String? = null,
    val imagemUrl: String? = null,

    val observacoes: String? = null,
    val categoria: String? = null
)

package com.example.apkstelladitalia20.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "carrinho")
data class ProdutoCarrinhoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    val idProduto: String = "",
    val nome: String = "",
    val valor: Double = 0.0,
    var quantidade: Int = 1,

    val tipo: String = "normal",         // "promocao" ou "normal"
    val descricao: String? = null,
    val imagemUrl: String? = null,

    val observacoes: String? = null,
    val categoria: String? = null,

    val tamanho: String? = null,
    val sabores: String? = null
) : Parcelable

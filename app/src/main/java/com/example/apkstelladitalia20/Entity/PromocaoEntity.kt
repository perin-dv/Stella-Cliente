package com.example.apkstelladitalia20.model

import android.os.Parcelable
import com.example.apkstelladitalia20.Entity.ProdutoAdicionalEntity
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class PromocaoEntity(
    val id: String = "",
    val idUsuario: String = "",
    val titulo: String = "",
    val observacao: String = "",
    val valor: Double = 0.0,
    val nome: String = "",
    val descricao: String = "",
    var quantidade: Int = 1,
    val imagemBase64: String = "",
    var produtos: List<ProdutoEntity> = emptyList(), // <--- mudou aqui!
    var produtosInclusos: List<ProdutoAdicionalEntity> = emptyList()
) : Parcelable



package com.example.apkstelladitalia20.Entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProdutoAdicionalEntity(
    val id: String = "",
    val nome: String = "",
    val valor: Double = 0.0,
    val imagemBase64: String? = null

) : Parcelable


package com.example.apkstelladitalia20.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PizzaResumo(
    val tamanho: String,
    val sabor1: String,
    val sabor2: String,
    val preco: Double,
    val imagem: String?=null
) : Parcelable
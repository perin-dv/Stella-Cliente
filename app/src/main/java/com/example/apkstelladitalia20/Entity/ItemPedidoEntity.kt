package com.example.apkstelladitalia20.Entity

import java.io.Serializable

data class ItemPedidoEntity(
    val nome: String = "",
    val quantidade: Int = 1,
    val preco: Double = 0.0,
    val imagemUrl: String = ""
) : Serializable

{
    fun getPrecoReal(): Double = preco
}
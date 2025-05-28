package com.example.apkstelladitalia20.model

import java.io.Serializable

/**
 * Representa uma bebida adicional para o carrinho.
 */
data class BebidaEntity(
    val nome: String = "",
    val preco: Double = 0.0,
    val imagem: String = ""
) : Serializable

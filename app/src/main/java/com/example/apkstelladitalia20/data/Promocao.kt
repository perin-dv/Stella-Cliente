package com.example.apkstelladitalia20.model

import java.io.Serializable

data class Promocao(
    val id: String = "",
    val idUsuario: String = "",
    val titulo: String = "",
    val observacao: String = "",
    val valor: Double = 0.0,
    val imagemBase64: String = "",
    val produtos: List<String> = emptyList()
) : Serializable

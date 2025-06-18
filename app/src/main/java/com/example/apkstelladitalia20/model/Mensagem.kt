package com.example.apkstelladitalia20.model


data class Mensagem(
    var id: String = "",
    var mensagem: String = "",
    var origem: String = "",
    var timestamp: Long = 0L,
    var tipo: String = "",
    var lida: Boolean = false,
    var autor: String = "",
    var horario: String = "",
    var foto: String = "",
    var status: String = "",
    val texto: String = "",
    val remetente: String = "",


)

package com.example.apkstelladitalia20.Entity

import androidx.room.Entity


@Entity(tableName = "endereco")
data class EnderecoEntity(
    var rua: String,
    var numero: String,
    var bairro: String,
    var cidade: String,
    var estado: String,
    var cep: String,
    var referencia: String
) {
    constructor() : this("", "", "", "", "", "", "")

}


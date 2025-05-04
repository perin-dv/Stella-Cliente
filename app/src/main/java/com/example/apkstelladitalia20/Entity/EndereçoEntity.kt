package com.example.apkstelladitalia20.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "endereco")
data class EnderecoEntity(
    var rua: String,
    var numero: String,
    var bairro: String,
    var cidade: String,
    var estado: String,
    var cep: String,
    var endereco: String,
    var telefone: String,
    var referencia: String
) {
    constructor() : this("", "", "", "", "", "", "", "", "")
}


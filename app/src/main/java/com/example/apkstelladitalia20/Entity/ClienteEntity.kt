package com.example.stelladitalia20.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey val uid: String,
    val nome: String,
    val email: String,
    val senha: String,
    val endereco: String,
    val telefone: String
) {
    constructor() : this("", "", "", "", "", "")
}

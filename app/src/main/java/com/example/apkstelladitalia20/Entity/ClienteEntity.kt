package com.example.stelladitalia20.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.apkstelladitalia20.Entity.EnderecoEntity

@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey val uid: String,
    var nome: String,
    var email: String,
    var senha: String,
    var telefone: String,
    var endereco: EnderecoEntity?=null
) {
    constructor() : this("", "", "", "", "",EnderecoEntity())
}

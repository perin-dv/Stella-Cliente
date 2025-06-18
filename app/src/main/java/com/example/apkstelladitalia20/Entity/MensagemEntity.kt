package com.example.apkstelladitalia20.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room local
@Entity(tableName = "mensagens")
data class MensagemEntity(
    @PrimaryKey val id: String,
    val mensagem: String,
    val origem: String,
    val timestamp: Long,
    val tipo: String,
    val lida: Boolean
)

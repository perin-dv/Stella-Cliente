package com.example.apkstelladitalia20.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversas")
data class ConversaEntity(
    @PrimaryKey val idPedido: String,
    val data: String = "",
    val valor: Double = 0.0,
    val status: String = "",
    val ultimaMensagem: String = "",
    val uidCliente: String = "",
    val titulo: String = "",
    val timestamp: Long = 0L  // ← este campo precisa existir!
)

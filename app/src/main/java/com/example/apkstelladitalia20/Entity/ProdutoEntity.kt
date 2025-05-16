package com.example.apkstelladitalia20.Entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
@Entity(tableName = "produto")
data class ProdutoEntity(
    @PrimaryKey val id: String = "",
    val nome: String = "",
    @field:JvmField
    @SerializedName("imagem")
    val imagem: String = "",
    val precoOriginal: Double = 0.0,
    val precoAtual: Double = 0.0,
    val maisPedido: Boolean = false,
    val quantidadeVendida: Int = 0,
    val categoria: String = "",
    val descricao: String = "",
    @field:JvmField
    @field:com.google.firebase.database.PropertyName("preco")
    val valor: Double = 0.0
) : Parcelable

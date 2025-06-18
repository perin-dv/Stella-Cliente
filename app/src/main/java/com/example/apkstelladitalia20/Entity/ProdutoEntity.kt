package com.example.apkstelladitalia20.Entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
@IgnoreExtraProperties
@Entity(tableName = "produto")
data class ProdutoEntity(
    @PrimaryKey var id: String = "",
    val nome: String = "",

    @SerializedName("imagem")
    val imagem: String? = "",

    val precoOriginal: Double = 0.0,
    val precoAtual: Double = 0.0,
    val maisPedido: Boolean = false,
    val quantidadeVendida: Int = 0,
    val categoria: String? = "",
    var descricao: String? = "",
    val idUsuario: String? = "",
    var quantidade: Int = 1,

    @get:com.google.firebase.database.PropertyName("preco")
    @set:com.google.firebase.database.PropertyName("preco")
    var valor: Double = 0.0
) : Parcelable, Serializable {

    // ✅ Fallback para sempre retornar o preço correto
    fun getPrecoReal(): Double {
        return when {
            precoAtual > 0 -> precoAtual
            valor > 0 -> valor
            precoOriginal > 0 -> precoOriginal
            else -> 0.0
        }
    }
}

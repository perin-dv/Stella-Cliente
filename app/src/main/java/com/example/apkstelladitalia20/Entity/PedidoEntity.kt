package com.example.apkstelladitalia20.Entity

import android.os.Parcelable
import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
@Suppress("DEPRECATED_ANNOTATION")
@Parcelize
data class PedidoEntity(
    var numero: String = "",
    val nomeLoja: String = "",
    val dataHora: String = "",
    val horaConfirmacao: String? = null,
    val status: String = "aguardando", // aguardando, confirmado, entrega, concluido
    val itens: List<ProdutoCarrinhoEntity> = emptyList(),
    val subtotal: Double = 0.0,
    val observacao: String = "",
    val desconto: Double = 0.0,
    val entrega: String = "Grátis",
    val total: Double = 0.0,
    val id: String = "",
    var formaPagamento: String = "Cartão de crédito",
    val enderecoEntrega: String = "",
    val observacoes: String = "",
    val foto: String = "",
    val cpfNota: String? = null,
    var avaliado: Boolean = false,
    var horaEntregaPrevista: String? = null,
    var empresaId: String = "",
    var email: String? = null,


    // ✅ NOVOS CAMPOS DO CLIENTE
    val clienteId: String = "",
    val nomeCliente: String = "",
    val telefoneCliente: String = ""
) : Parcelable



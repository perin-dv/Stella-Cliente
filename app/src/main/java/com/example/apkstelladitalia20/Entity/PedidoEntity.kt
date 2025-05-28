package com.example.apkstelladitalia20.Entity

import java.io.Serializable

data class PedidoEntity(
    val numero: Int = 0,
    val nomeLoja: String = "",
    val dataHora: String = "",
    val horaConfirmacao: String? = null,
    val status: String = "aguardando", // aguardando, confirmado, entrega, concluido
    val itens: List<ProdutoEntity> = emptyList(),
    val subtotal: Double = 0.0,
    val desconto: Double = 0.0,
    val entrega: String = "Grátis",
    val total: Double = 0.0,
    val formaPagamento: String = "Cartão de crédito",
    val enderecoEntrega: String = "",
    val observacoes: String = "",
    val foto: String = "",


    // ✅ NOVOS CAMPOS DO CLIENTE
    val clienteId: String = "",
    val nomeCliente: String = "",
    val telefoneCliente: String = ""
) : Serializable



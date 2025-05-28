package com.example.apkstelladitalia20.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carrinho")
data class ProdutoCarrinhoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val idProduto: String,           // Pode ser produto ou promoção
    val nome: String,
    val valor: Double,
    val quantidade: Int = 1,

    val tipo: String = "normal",     // "normal" ou "promocao"
    val descricao: String? = null,   // Ex: descrição da promoção
    val imagemUrl: String? = null,   // Pode ser usado para mostrar imagem da promo/produto

    // Campos adicionais úteis para pizza
    val tamanho: String? = null,
    val sabores: String? = null,     // Ex: "Calabresa + Frango"
    val observacoes: String? = null  // Observações extras
)

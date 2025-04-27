package com.example.apkstelladitalia20.model

import java.io.Serializable

data class ItemCarrinhoPromocao(
    val promocao: PromocaoEntity,
    var quantidade: Int
) : Serializable

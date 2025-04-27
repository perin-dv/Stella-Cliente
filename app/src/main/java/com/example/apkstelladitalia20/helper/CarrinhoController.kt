package com.example.apkstelladitalia20.helper

import com.example.apkstelladitalia20.model.ItemCarrinhoPromocao
import com.example.apkstelladitalia20.model.PromocaoEntity

object CarrinhoController {

    private val itensCarrinhoPromocao = mutableListOf<ItemCarrinhoPromocao>()

    fun adicionarItem(promocao: PromocaoEntity, quantidade: Int) {
        itensCarrinhoPromocao.add(ItemCarrinhoPromocao(promocao, quantidade))
    }

    fun listarItens(): List<ItemCarrinhoPromocao> {
        return itensCarrinhoPromocao
    }

    fun limparCarrinho() {
        itensCarrinhoPromocao.clear()
    }
}

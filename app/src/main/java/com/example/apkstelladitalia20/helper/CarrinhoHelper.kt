package com.example.apkstelladitalia20.helper

import android.content.Context
import com.example.apkstelladitalia20.Entity.ProdutoEntity

object CarrinhoHelper {

    private val carrinho = mutableListOf<ProdutoEntity>()

    fun adicionar(produto: ProdutoEntity) {
        val existente = carrinho.find { it.nome == produto.nome }
        if (existente != null) {
            existente.quantidade += produto.quantidade
        } else {
            carrinho.add(produto)
        }
    }

    fun remover(produto: ProdutoEntity) {
        carrinho.removeIf { it.nome == produto.nome }
    }

    fun limpar() {
        carrinho.clear()
    }

    fun atualizar(produto: ProdutoEntity) {
        val index = carrinho.indexOfFirst { it.nome == produto.nome }
        if (index != -1) {
            carrinho[index] = produto
        }
    }

    fun getCarrinho(): List<ProdutoEntity> {
        return carrinho
    }

    fun getTotal(): Double {
        return carrinho.sumOf { it.valor * it.quantidade }
    }

    fun temItens(): Boolean {
        return carrinho.isNotEmpty()
    }
}

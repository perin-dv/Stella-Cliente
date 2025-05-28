package com.example.apkstelladitalia20.controller

import com.example.apkstelladitalia20.Entity.ProdutoEntity

object CarrinhoController {

    private val listaCarrinho = mutableListOf<ProdutoEntity>()

    fun adicionar(item: ProdutoEntity) {
        val existente = listaCarrinho.find { it.nome == item.nome }
        if (existente != null) {
            existente.quantidade += item.quantidade
        } else {
            listaCarrinho.add(item)
        }
    }

    fun remover(item: ProdutoEntity) {
        listaCarrinho.removeIf { it.nome == item.nome }
    }

    fun limparCarrinho() {
        listaCarrinho.clear()
    }

    fun atualizarQuantidade(item: ProdutoEntity, novaQuantidade: Int) {
        val index = listaCarrinho.indexOfFirst { it.nome == item.nome }
        if (index != -1) {
            if (novaQuantidade <= 0) {
                listaCarrinho.removeAt(index)
            } else {
                listaCarrinho[index].quantidade = novaQuantidade
            }
        }
    }

    fun getCarrinho(): List<ProdutoEntity> {
        return listaCarrinho
    }

    fun getTotal(): Double {
        return listaCarrinho.sumOf { it.valor * it.quantidade }
    }

    fun getQuantidadeItens(): Int {
        return listaCarrinho.sumOf { it.quantidade }
    }

    fun existeNoCarrinho(): Boolean {
        return listaCarrinho.isNotEmpty()
    }
    fun getItens(): List<ProdutoEntity> {
        return listaCarrinho.toList()
    }
}

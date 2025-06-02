package com.example.apkstelladitalia20.controller

import androidx.lifecycle.LiveData
import com.example.apkstelladitalia20.Entity.ProdutoEntity

object CarrinhoController {

    private val listaCarrinho = mutableListOf<ProdutoEntity>()
    private val carrinhoLiveData = androidx.lifecycle.MutableLiveData<List<ProdutoEntity>>(listaCarrinho)


    fun adicionar(item: ProdutoEntity) {
        val existente = listaCarrinho.find { it.nome == item.nome }

        if (existente != null) {
            existente.quantidade += item.quantidade
        } else {
            listaCarrinho.add(item)
        }

        carrinhoLiveData.value = listaCarrinho.toList() // sempre atualiza a lista com cópia para notificar o observer
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

    fun getCarrinho(): LiveData<List<ProdutoEntity>> = carrinhoLiveData

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

package com.example.apkstelladitalia20.data

import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity
import androidx.lifecycle.LiveData
import com.example.apkstelladitalia20.data.CarrinhoDao

class CarrinhoRepository(private val dao: CarrinhoDao) {

    val itensCarrinho: LiveData<List<ProdutoCarrinhoEntity>> = dao.getCarrinho()

    suspend fun adicionar(produto: ProdutoCarrinhoEntity) {
        dao.inserirProduto(produto)
    }

    suspend fun limparCarrinho() {
        dao.limparCarrinho()
    }


    suspend fun removerPorNome(nome: String) {
        dao.removerPorNome(nome)
    }

}

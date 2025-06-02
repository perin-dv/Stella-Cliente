package com.example.apkstelladitalia20.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.data.CarrinhoRepository
import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity
import com.example.apkstelladitalia20.data.AppDatabase
import kotlinx.coroutines.launch

class CarrinhoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CarrinhoRepository
    val itensCarrinho: LiveData<List<ProdutoCarrinhoEntity>>



    init {

        val dao = AppDatabase.getDatabase(application).carrinhoDao()
        repository = CarrinhoRepository(dao)
        itensCarrinho = repository.itensCarrinho
    }

    fun adicionar(produto: ProdutoCarrinhoEntity) {
        viewModelScope.launch {
            repository.adicionar(produto)

        }
    }


    fun limparCarrinho() {
        viewModelScope.launch {
            repository.limparCarrinho()
        }
    }

    fun removerPorNome(nome: String) {
        viewModelScope.launch {
            repository.removerPorNome(nome)
        }
    }

    fun salvarItem(produto: ProdutoEntity) {
        viewModelScope.launch {
            val item = ProdutoCarrinhoEntity(
                idProduto = produto.id,
                nome = produto.nome,
                valor = produto.getPrecoReal(),
                quantidade = 1,
                descricao = produto.descricao ?: "",
                imagemUrl = produto.imagem,
                tipo = "produto"
            )
            repository.adicionar(item)
        }
    }
    fun salvarPromocao(promo: PromocaoEntity) {
        viewModelScope.launch {
            val item = ProdutoCarrinhoEntity(
                idProduto = "promo_${promo.id}", // ← chave estável
                nome = promo.titulo.ifEmpty { "Promoção Especial" },
                valor = promo.valor.takeIf { it > 0.0 } ?: promo.produtos?.sumOf { it.valor } ?: 0.0,
                quantidade = 1,
                descricao = promo.observacao ?: "",
                imagemUrl = promo.imagemBase64,
                tipo = "promocao"
            )
            repository.adicionar(item)
        }
    }


}

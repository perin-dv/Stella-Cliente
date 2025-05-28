package com.example.apkstelladitalia20.model

import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.apkstelladitalia20.data.CarrinhoRepository
import com.example.stelladitaliaempresa.data.AppDatabase
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


}

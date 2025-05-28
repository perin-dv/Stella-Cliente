package com.example.apkstelladitalia20.data

import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

import androidx.room.Query

@Dao
interface CarrinhoDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun inserirProduto(produto: ProdutoCarrinhoEntity)

    @Query("SELECT * FROM carrinho")
    fun getCarrinho(): LiveData<List<ProdutoCarrinhoEntity>>

    @Query("DELETE FROM carrinho")
    suspend fun limparCarrinho()

    @Query("DELETE FROM carrinho WHERE nome = :nome")
    suspend fun removerPorNome(nome: String)

}
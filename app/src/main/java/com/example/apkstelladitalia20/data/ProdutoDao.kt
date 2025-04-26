package com.example.stelladitaliaempresa.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

import com.example.apkstelladitalia20.Entity.ProdutoEntity

@Dao
interface ProdutoDao {

    @Insert
    suspend fun inserirProduto(produto: ProdutoEntity)

    @Query("SELECT * FROM produto WHERE categoria = :categoria")
    fun buscarPorCategoria(categoria: String): List<ProdutoEntity>

    @Query("SELECT * FROM produto WHERE id = :id")
    fun buscarPorId(id: String): ProdutoEntity?

    @Query("DELETE FROM produto WHERE id = :id")
    suspend fun deletarPorId(id: String)

    @Query("SELECT * FROM produto")
    fun listarTodos(): List<ProdutoEntity>
}

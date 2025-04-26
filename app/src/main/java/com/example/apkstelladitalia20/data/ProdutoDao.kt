package com.example.stelladitaliaempresa.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

import com.stelladitalia.model.Produto

@Dao
interface ProdutoDao {

    @Insert
    suspend fun inserirProduto(produto: Produto)

    @Query("SELECT * FROM produto WHERE categoria = :categoria")
    fun buscarPorCategoria(categoria: String): List<Produto>

    @Query("SELECT * FROM produto WHERE id = :id")
    fun buscarPorId(id: String): Produto?

    @Query("DELETE FROM produto WHERE id = :id")
    suspend fun deletarPorId(id: String)

    @Query("SELECT * FROM produto")
    fun listarTodos(): List<Produto>
}

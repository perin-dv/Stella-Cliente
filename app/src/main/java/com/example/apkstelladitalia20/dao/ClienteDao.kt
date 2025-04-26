package com.example.stelladitaliaempresa.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stelladitalia20.Entity.ClienteEntity

@Dao
interface ClienteDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserir(cliente: ClienteEntity)

    @Query("SELECT * FROM clientes WHERE uid = :uid LIMIT 1")
    suspend fun buscarPorUid(uid: String): ClienteEntity?

    @Query("SELECT * FROM clientes")
    suspend fun getTodos(): List<ClienteEntity>


    @Query("SELECT * FROM clientes WHERE email = :email AND senha = :senha")
    suspend fun getClienteByEmailAndSenha(email: String, senha: String): ClienteEntity?
}

package com.example.apkstelladitalia20.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.apkstelladitalia20.Entity.ConversaEntity


@Dao
interface ConversaDao {

    @Query("SELECT * FROM conversas ORDER BY data DESC")
    fun getTodas(): LiveData<List<ConversaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(conversa: ConversaEntity)
}

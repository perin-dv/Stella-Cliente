package com.example.stelladitaliaempresa.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.stelladitalia20.Entity.ClienteEntity
import com.example.stelladitaliaempresa.dao.ClienteDao
import com.stelladitalia.model.Produto

@Database(entities = [ClienteEntity::class, Produto::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clienteDao(): ClienteDao
    abstract fun produtoDao(): ProdutoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stella_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

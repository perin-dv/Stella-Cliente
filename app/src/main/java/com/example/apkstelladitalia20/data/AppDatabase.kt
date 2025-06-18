package com.example.apkstelladitalia20.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.apkstelladitalia20.Entity.ConversaEntity
import com.example.stelladitalia20.Entity.ClienteEntity
import com.example.stelladitaliaempresa.dao.ClienteDao
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.activity.ChatActivity
import com.example.apkstelladitalia20.database.dao.ConversaDao
import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity


@Database(
    entities = [
        ClienteEntity::class,
        ProdutoEntity::class,
        ProdutoCarrinhoEntity::class,
        ConversaEntity::class,
    ],
    version = 25, exportSchema = false

)
abstract class AppDatabase : RoomDatabase() {


    abstract fun carrinhoDao(): CarrinhoDao
    abstract fun clienteDao(): ClienteDao
    abstract fun produtoDao(): ProdutoDao
    abstract fun conversaDao(): ConversaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stella_db"

                ).fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

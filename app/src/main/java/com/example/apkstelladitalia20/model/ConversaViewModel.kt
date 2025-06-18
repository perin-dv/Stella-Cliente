package com.example.apkstelladitalia20.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.apkstelladitalia20.Entity.ConversaEntity
import com.example.apkstelladitalia20.data.AppDatabase


class ConversaViewModel(application: Application) : AndroidViewModel(application) {

    private val conversaDao = AppDatabase.getDatabase(application).conversaDao()

    fun getConversas(): LiveData<List<ConversaEntity>> {
        return conversaDao.getTodas()
    }
}

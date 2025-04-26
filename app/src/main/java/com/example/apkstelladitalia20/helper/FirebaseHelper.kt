package com.example.apkstelladitalia20.helper

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

object FirebaseHelper {

    val database: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    fun empresaDatabase(context: Context): DatabaseReference {
        return FirebaseDatabase.getInstance().reference
    }
}

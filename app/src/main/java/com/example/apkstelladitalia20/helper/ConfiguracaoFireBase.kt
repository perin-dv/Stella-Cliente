package com.example.apkstelladitalia20.helper

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class ConfiguracaoFireBase {
    companion object {
        val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
        val db: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
        val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    }
}

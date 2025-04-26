package com.example.apkstelladitalia20.helper

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

object FirebaseHelper {

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val clienteDatabase: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private var empresaApp: FirebaseApp? = null
    private var empresaDbRef: DatabaseReference? = null

    fun empresaDatabase(context: Context): DatabaseReference {
        if (empresaApp == null) {
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:1085571235178:android:2f353fce63f6785b30f10f")
                .setApiKey("AIzaSyDsW9UzEflhTtDFbCcRk3Z0gnxI6qpkdF4")
                .setDatabaseUrl("https://stella-d-italia-default-rtdb.firebaseio.com/")
                .setProjectId("stelladitaliaempresa")
                .build()

            empresaApp = FirebaseApp.initializeApp(context, options, "empresaApp")
        }

        if (empresaDbRef == null) {
            empresaDbRef = FirebaseDatabase.getInstance(empresaApp!!).reference
        }

        return empresaDbRef!!
    }

    fun getIdUsuario(): String? {
        return auth.currentUser?.uid
    }

    fun isAutenticado(): Boolean {
        return auth.currentUser != null
    }
}

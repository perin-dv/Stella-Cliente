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
            try {
                empresaApp = FirebaseApp.getApps(context).firstOrNull { it.name == "empresaApp" }
                    ?: FirebaseApp.initializeApp(
                        context,
                        FirebaseOptions.Builder()
                            .setDatabaseUrl("https://stella-d-italia-default-rtdb.firebaseio.com/")
                            .build(),
                        "empresaApp"
                    )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (empresaDbRef == null && empresaApp != null) {
            empresaDbRef = FirebaseDatabase.getInstance(empresaApp!!).reference
        }

        return empresaDbRef ?: FirebaseDatabase.getInstance().reference
    }


    fun getIdUsuario(): String? {
        return auth.currentUser?.uid
    }

    fun isAutenticado(): Boolean {
        return auth.currentUser != null
    }
}

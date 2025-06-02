package com.example.apkstelladitalia20.data

import android.content.Context
import com.example.apkstelladitalia20.Entity.EnderecoEntity
import com.example.apkstelladitalia20.model.LoggedInUser
import com.example.stelladitalia20.Entity.ClienteEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

class LoginRepository(private val context: Context) {

    private val firebaseRef = FirebaseDatabase.getInstance().getReference("clientes")

    suspend fun login(email: String, senha: String): Result<LoggedInUser> =
        withContext(Dispatchers.IO) {
            try {
                val auth = FirebaseAuth.getInstance()
                val result = auth.signInWithEmailAndPassword(email, senha).await()
                val firebaseUser = result.user

                if (firebaseUser != null) {
                    val uid = firebaseUser.uid
                    val userSnapshot = firebaseRef.child(uid).get().await()

                    if (userSnapshot.exists()) {
                        val nome = userSnapshot.child("nome").getValue(String::class.java) ?: ""
                        val endereco =
                            userSnapshot.child("endereco").getValue(String::class.java) ?: ""
                        val telefone =
                            userSnapshot.child("telefone").getValue(String::class.java) ?: ""

                        val cliente = ClienteEntity(
                            uid = uid,
                            nome = nome,
                            email = email,
                            senha = senha,
                            endereco = EnderecoEntity(),
                            telefone = telefone,
                                                 )

                        // Salva localmente no Room
                        val clienteDao = AppDatabase.getDatabase(context).clienteDao()
                        clienteDao.inserir(cliente)

                        return@withContext Result.Success(
                            LoggedInUser(
                                userId = uid,
                                displayName = nome
                            )
                        )
                    } else {
                        return@withContext Result.Error(IOException("Dados do usuário não encontrados no Firebase"))
                    }
                } else {
                    return@withContext Result.Error(IOException("Falha ao autenticar com o Firebase"))
                }

            } catch (e: Exception) {
                return@withContext Result.Error(IOException("Erro no login", e))
            }
        }
}
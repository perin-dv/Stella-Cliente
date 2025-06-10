package com.example.apkstelladitalia20.Entity

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
@Keep
data class ClienteFirebase(
    var uid: String = "",
    var nome: String = "",
    var email: String = "",
    var senha: String = "",
    var telefone: String = "",
    var endereco: EnderecoEntity?=null
)

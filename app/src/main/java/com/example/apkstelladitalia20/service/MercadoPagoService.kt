package com.example.apkstelladitalia20.service

import com.example.apkstelladitalia20.model.CardTokenRequest
import com.example.apkstelladitalia20.model.CardTokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface MercadoPagoService {
    @POST("v1/card_tokens?public_key=APP_USR-ceae97b0-ec68-4b4e-9d35-0364a38ed2bd")
    fun criarToken(@Body card: CardTokenRequest): Call<CardTokenResponse>
}

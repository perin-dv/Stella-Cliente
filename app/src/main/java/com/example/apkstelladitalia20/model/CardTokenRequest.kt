package com.example.apkstelladitalia20.model

data class CardTokenRequest(
    val card_number: String,
    val expiration_month: Int,
    val expiration_year: Int,
    val security_code: String,
    val cardholder: Cardholder
)

data class Cardholder(
    val name: String
)

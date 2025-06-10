package com.example.apkstelladitalia20.model

data class CardTokenResponse(
    val id: String,
    val last_four_digits: String,
    val card_number_length: Int,
    val cardholder: CardholderInfo
)

data class CardholderInfo(
    val name: String
)

package com.example.apkstelladitalia20.util

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.apkstelladitalia20.Entity.PedidoEntity
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun PedidoEntity.estaAtrasado(): Boolean {
    if (horaEntregaPrevista.isNullOrEmpty()) return false

    return try {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val horaPrevista = LocalTime.parse(horaEntregaPrevista, formatter)
        val agora = LocalTime.now()
        agora.isAfter(horaPrevista) && status != "concluido"
    } catch (e: Exception) {
        false
    }
}

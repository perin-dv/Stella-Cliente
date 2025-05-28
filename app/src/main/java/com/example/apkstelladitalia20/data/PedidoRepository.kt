package com.example.apkstelladitalia20.repository

import android.content.Context
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object PedidoRepository {

    fun salvarPedido(context: Context, pedido: PedidoEntity, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val prefs = context.getSharedPreferences("appStella", Context.MODE_PRIVATE)
        val uidEmpresa = prefs.getString("uidEmpresa", null)
        if (uidEmpresa == null) {
            onError(Exception("Empresa não encontrada"))
            return
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("empresa")
            .child(uidEmpresa)
            .child("pedidos")
            .push()

        ref.setValue(pedido)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }

    fun getPedidos(): List<PedidoEntity> {
        return listOf(
            PedidoEntity(
                numero = 1234,
                dataHora = "10:45",
                status = "confirmado",
                total = 58.0,
                itens = listOf(
                    ProdutoEntity(nome = "Pizza Calabresa", quantidade = 2),
                    ProdutoEntity(nome = "Coca-Cola", quantidade = 1)
                ),
                nomeLoja = "Stella D’Italia"
            )
        )
    }

    fun salvarPedidoDoCliente(pedido: PedidoEntity) {
        val uidCliente = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseDatabase.getInstance()
            .getReference("clientes")
            .child(uidCliente)
            .child("pedidos")
            .push()

        ref.setValue(pedido)
    }
}

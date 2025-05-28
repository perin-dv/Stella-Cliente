package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.adapter.ItemPedidoAdapter
import com.example.apkstelladitalia20.controller.CarrinhoController
import com.example.apkstelladitalia20.databinding.ActivityDetalhesPedidoBinding

class DetalhesPedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesPedidoBinding
    private lateinit var pedido: PedidoEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carregarPedido()
        configurarLayout()
    }

    private fun carregarPedido() {
        pedido = intent.getSerializableExtra("pedido") as? PedidoEntity
            ?: return finish() // fecha se falhar

        binding.tvNomeLoja.text = pedido.nomeLoja
        binding.tvNumeroPedido.text = "Pedido nº ${pedido.numero} • ${pedido.dataHora}"

        // Status + ícone
        binding.tvStatusPedido.text = when (pedido.status) {
            "concluido" -> "Pedido concluído às ${pedido.horaConfirmacao}"
            "confirmado" -> "Pedido confirmado"
            "entrega" -> "Saiu para entrega"
            else -> "Aguardando confirmação"
        }

        // Itens
        val adapter = ItemPedidoAdapter(pedido.itens)
        binding.recyclerItens.adapter = adapter

        // Valores
        val subtotalRecalculado = pedido.itens.sumOf { it.getPrecoReal() * it.quantidade }
        binding.tvResumoSubtotal.text = "Subtotal: R$ %.2f".format(subtotalRecalculado)
        binding.tvResumoDescontos.text = "Descontos: - R$ %.2f".format(pedido.desconto)
        binding.tvResumoEntrega.text = "Entrega: ${pedido.entrega}"
        binding.tvResumoTotal.text = "Total: R$ %.2f".format(pedido.total)

        // Pagamento
        binding.tvMetodoPagamento.text = pedido.formaPagamento

        // Endereço
        binding.tvEnderecoEntrega.text = pedido.enderecoEntrega

        // Mostrar avaliação se status = concluído
        if (pedido.status == "concluido") {
            binding.boxAvaliacao.visibility = View.VISIBLE
        } else {
            binding.boxAvaliacao.visibility = View.GONE
        }

        // Repetir pedido
        binding.btnPedirNovamente.setOnClickListener {
            repetirPedido(pedido)
        }
    }

    private fun configurarLayout() {
        supportActionBar?.title = "Detalhes do Pedido"
    }

    private fun repetirPedido(pedido: PedidoEntity) {
        // Simples: reenvia os itens para o carrinho
        for (item in pedido.itens) {
            CarrinhoController.adicionar(item)
        }

        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("abrirCarrinho", true)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }
}

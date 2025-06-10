package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.adapter.ItemResumoPedidoAdapter
import com.example.apkstelladitalia20.databinding.ActivityDetalhesPedidoBinding
import com.example.apkstelladitalia20.model.CarrinhoViewModel
import com.example.apkstelladitalia20.util.estaAtrasado
import com.google.firebase.database.FirebaseDatabase

class DetalhesPedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesPedidoBinding
    private lateinit var pedido: PedidoEntity
    private lateinit var viewModel: CarrinhoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CarrinhoViewModel::class.java]

        val pedidoId = intent.getStringExtra("idPedido") ?: return finish()
        val clienteId = intent.getStringExtra("clienteId") ?: return finish()

        carregarPedidoDoFirebase(pedidoId, clienteId)
    }

    private fun carregarPedidoDoFirebase(pedidoId: String, clienteId: String) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("clientes")
            .child(clienteId)
            .child("pedidos")
            .child(pedidoId)

        ref.get().addOnSuccessListener { snapshot ->
            val pedidoCarregado = snapshot.getValue(PedidoEntity::class.java)
            if (pedidoCarregado != null) {
                pedido = pedidoCarregado
                carregarPedido()
                configurarLayout()
                atualizarLinhaStatus(pedido.status ?: "")
            } else {
                Toast.makeText(this, "Pedido não encontrado!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao carregar pedido!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun carregarPedido() {
        binding.tvNomeLoja.text = "Stella D’Italia - Maringá"
        binding.tvNumeroPedido.text = "Pedido nº ${pedido.numero} • ${pedido.dataHora}"

        binding.tvStatusPedido.text = when (pedido.status) {
            "concluido" -> "Pedido concluído às ${pedido.horaConfirmacao}"
            "confirmado" -> "Pedido confirmado"
            "entrega" -> "Saiu para entrega"
            else -> "Aguardando confirmação"
        }

        val uidEmpresa = "stella" // ou recupere dinamicamente do SharedPreferences
        binding.recyclerItens.adapter = ItemResumoPedidoAdapter(pedido.itens, uidEmpresa)


        val subtotal = pedido.itens.sumOf { it.valor * it.quantidade }
        binding.tvResumoSubtotal.text = "Subtotal: R$ %.2f".format(subtotal)
        binding.tvResumoDescontos.text = "Descontos: - R$ %.2f".format(pedido.desconto)
        binding.tvResumoEntrega.text = when {
            pedido.entrega.isNullOrBlank() ||
                    pedido.entrega.equals("0.0", ignoreCase = true) ||
                    pedido.entrega.equals("grátis", ignoreCase = true) ->
                "Entrega: Grátis"
            else -> {
                val valorEntrega = pedido.entrega.toDoubleOrNull()
                if (valorEntrega != null && valorEntrega > 0)
                    "Entrega: R$ %.2f".format(valorEntrega)
                else
                    "Entrega: Grátis"
            }
        }

        binding.tvResumoTotal.text = "Total: R$ %.2f".format(pedido.total)
        binding.tvMetodoPagamento.text = pedido.formaPagamento ?: "-"

        val enderecoFormatado = pedido.enderecoEntrega?.let {
            it.removePrefix("{").removeSuffix("}").split(", ").associate {
                val (k, v) = it.split("=")
                k to v
            }
        }?.let {
            "${it["rua"]}, ${it["numero"]} - ${it["bairro"]}\n${it["cidade"]} - ${it["estado"]}, CEP: ${it["cep"]}\nReferência: ${it["referencia"]}"
        } ?: "-"

        binding.tvEnderecoEntrega.text = enderecoFormatado

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pedido.estaAtrasado()) {
            binding.tvAtrasado.visibility = View.VISIBLE
            binding.btnChatAtraso.visibility = View.VISIBLE
            binding.btnLigarLoja.visibility = View.VISIBLE
        } else {
            binding.tvAtrasado.visibility = View.GONE
            binding.btnChatAtraso.visibility = View.GONE
            binding.btnLigarLoja.visibility = View.GONE
        }

        binding.btnRastrearEntrega.setOnClickListener {
            val intent = Intent(this, EntregaTrackingActivity::class.java)
            intent.putExtra("idPedido", pedido.id)
            startActivity(intent)
        }

        if (pedido.status == "concluido") {
            val ref = FirebaseDatabase.getInstance()
                .getReference("avaliacoes")
                .child(pedido.id)

            ref.get().addOnSuccessListener { snapshot ->
                val estrelas = snapshot.child("estrelas").value?.toString()?.toFloatOrNull()
                if (estrelas != null) {
                    binding.boxAvaliacao.visibility = View.VISIBLE
                    binding.ratingAvaliacao.rating = estrelas
                    binding.ratingAvaliacao.setIsIndicator(true)
                } else {
                    binding.boxAvaliacao.visibility = View.VISIBLE
                    binding.ratingAvaliacao.setIsIndicator(false)
                    binding.ratingAvaliacao.setOnRatingBarChangeListener { _, rating, _ ->
                        if (rating > 0) salvarAvaliacao(pedido.id, rating.toInt())
                    }
                }
            }
        } else {
            binding.boxAvaliacao.visibility = View.GONE
        }

        if (pedido.status == "concluido") {
            binding.btnPedirNovamente.visibility = View.VISIBLE
            binding.btnPedirNovamente.setOnClickListener {
                repetirPedido(pedido)
            }
        } else {
            binding.btnPedirNovamente.visibility = View.GONE
        }

        binding.btnChatAtraso.setOnClickListener {
            Toast.makeText(this, "Abrir chat com a loja...", Toast.LENGTH_SHORT).show()
        }

        binding.btnLigarLoja.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:44999999999")
            startActivity(intent)
        }
    }


    private fun atualizarLinhaStatus(status: String) {
        val progresso: Int
        val cor: Int
        val textoExplicativo: String

        when (status.lowercase()) {
            "aguardando" -> {
                progresso = 25
                cor = R.color.vermelho
                textoExplicativo = "Aguardando a empresa confirmar seu pedido"
            }
            "confirmado" -> {
                progresso = 50
                cor = R.color.green
                textoExplicativo = "Seu pedido foi confirmado pela loja"
            }
            "entrega" -> {
                progresso = 75
                cor = R.color.blue
                textoExplicativo = "O entregador está a caminho"
            }
            "concluido" -> {
                progresso = 100
                cor = R.color.green
                textoExplicativo = "Pedido finalizado com sucesso"
            }
            else -> {
                progresso = 0
                cor = R.color.gray
                textoExplicativo = "-"
            }
        }

        // TRAVA o drawable e cor ANTES e DEPOIS
        binding.progressoStatus.apply {
            progress = 0 // Zera para evitar bug visual
            progressDrawable = null // Remove drawable atual para forçar reset
            progressDrawable = ContextCompat.getDrawable(context, R.drawable.progress_gourmet_drawable)
            progressTintList = ContextCompat.getColorStateList(context, cor)
            post {
                progress = progresso
            }
        }

        binding.tvLinhaStatusCinza.text = textoExplicativo
    }


    private fun configurarLayout() {
        supportActionBar?.title = "Detalhes do Pedido"
    }

    private fun repetirPedido(pedido: PedidoEntity) {
        for (item in pedido.itens) {
            viewModel.adicionar(item)
        }
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("abrirCarrinho", true)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    private fun salvarAvaliacao(pedidoId: String, estrelas: Int) {
        val ref = FirebaseDatabase.getInstance().getReference("avaliacoes").child(pedidoId)
        val avaliacao = mapOf("estrelas" to estrelas, "data" to System.currentTimeMillis())

        ref.setValue(avaliacao)
            .addOnSuccessListener {
                Toast.makeText(this, "Avaliação salva! Obrigado!", Toast.LENGTH_SHORT).show()
                binding.boxAvaliacao.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar avaliação", Toast.LENGTH_SHORT).show()
            }
    }
}

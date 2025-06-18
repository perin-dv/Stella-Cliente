package com.example.apkstelladitalia20.activity

import android.content.*
import android.graphics.BitmapFactory
import android.os.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.apkstelladitalia20.databinding.ActivityPagamentoPixBinding
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.*
import android.util.Base64
import android.util.Log
import com.google.firebase.FirebaseApp
import android.content.ClipData
import android.content.ClipboardManager
import com.example.apkstelladitalia20.Entity.PedidoEntity
import com.example.apkstelladitalia20.model.CarrinhoViewModel
import com.google.firebase.database.FirebaseDatabase

class PagamentoPixActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPagamentoPixBinding
    private lateinit var viewModel: CarrinhoViewModel
    private lateinit var functions: FirebaseFunctions
    private var paymentId: String? = null
    private var pollingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPagamentoPixBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase init (só por precaução)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
            functions = FirebaseFunctions.getInstance("us-central1")
        }

        // ViewModel
        viewModel = ViewModelProvider(this)[CarrinhoViewModel::class.java]
        functions = FirebaseFunctions.getInstance()

        // Toolbar
        setSupportActionBar(binding.toolbarPix)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarPix.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Recebe dados via intent
        val qrBase64 = intent.getStringExtra("qr_base64")
        val qrString = intent.getStringExtra("qr_string")
        paymentId = intent.getStringExtra("payment_id")

        // Exibe QR Code
        qrBase64?.let {
            val imageBytes = Base64.decode(it, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            binding.imgQrCode.setImageBitmap(bitmap)
        }

        // Exibe código Pix
        binding.txtCodigoPix.text = qrString ?: ""

        // Copiar código Pix
        binding.btnCopiarCodigo.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Pix", qrString)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Código Pix copiado!", Toast.LENGTH_SHORT).show()
        }

        iniciarVerificacaoPagamento()
    }

    private fun iniciarVerificacaoPagamento() {
        paymentId?.let {
            pollingJob = CoroutineScope(Dispatchers.Main).launch {
                while (isActive) {
                    verificarStatusPagamento(it)
                    delay(5000)
                }
            }
        }
    }

    private fun verificarStatusPagamento(paymentId: String) {
        val data = hashMapOf("paymentId" to paymentId)

        functions.getHttpsCallable("verificarStatusPagamento")
            .call(data)
            .addOnSuccessListener { result ->
                val map = result.data as? Map<*, *>
                val status = map?.get("status")?.toString()
                val statusDetail = map?.get("statusDetail")?.toString()

                if (status == "approved") {
                    val pedidoTemp = intent.getSerializableExtra("pedidoTemp") as? PedidoEntity
                    if (pedidoTemp != null) {
                        salvarPedidoNoFirebase(pedidoTemp)
                        viewModel.limparCarrinho()
                    }

                    pollingJob?.cancel()
                    Toast.makeText(this, "Pagamento aprovado!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("abrirAba", "pedidos")
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else if (status != null && status != "in_process") {
                    // Se não for aprovado nem pendente, mostra erro
                    val mensagem = traduzirErroStatus(statusDetail)
                    Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
                    pollingJob?.cancel()
                } else {
                    Log.d("PixStatus", "Aguardando pagamento. Status atual: $status")
                }
            }
            .addOnFailureListener {
                Log.e("PixStatus", "Erro ao verificar pagamento: ${it.message}")
            }
    }

    private fun salvarPedidoNoFirebase(pedido: PedidoEntity) {
        val database = FirebaseDatabase.getInstance()
        val id = pedido.id

        // Cliente
        database.getReference("pedidos_confirmados_clientes")
            .child(pedido.clienteId)
            .child(id)
            .setValue(pedido)

        // Empresa
        database.getReference("pedidos_confirmados")
            .child(pedido.empresaId)
            .child(id)
            .setValue(pedido)
    }

    fun traduzirErroStatus(codigo: String?): String {
        return when (codigo) {
            "cc_rejected_call_for_authorize", "CALL" -> "Pagamento recusado: é necessário autorizar com o banco."
            "cc_rejected_insufficient_amount", "FUND" -> "Pagamento recusado: saldo insuficiente."
            "cc_rejected_bad_filled_security_code", "SECU" -> "Pagamento recusado: código de segurança inválido."
            "cc_rejected_bad_filled_expiration_date", "EXPI" -> "Pagamento recusado: validade inválida."
            "cc_rejected_other_reason", "OTHE" -> "Pagamento recusado: erro geral. Tente outro cartão."
            "cc_rejected_bad_filled_card_number", "FORM" -> "Erro no número do cartão."
            "pending_contingency", "CONT" -> "Pagamento pendente. Aguarde confirmação do banco."
            else -> "Pagamento não aprovado. Tente novamente."
        }
    }

    override fun onDestroy() {
        pollingJob?.cancel()
        super.onDestroy()
    }
}

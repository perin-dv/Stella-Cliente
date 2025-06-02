const functions = require("firebase-functions");
const admin = require("firebase-admin");
const mercadopago = require("mercadopago");

require("dotenv").config();
admin.initializeApp();

// 🔐 Configurar token do ambiente Firebase
mercadopago.configure({
  access_token: functions.config().mercadopago.token
});

// ✅ Função 1 – Gerar pagamento direto via Pix com QR Code
exports.criarPagamentoPix = functions.https.onCall(async (data, context) => {
  try {
    const { idEmpresa, nomeCliente, telefoneCliente, enderecoEntrega, valorTotal, pedidoId } = data;
    const empresaSnap = await admin.database().ref(`empresa/${idEmpresa}`).once("value");
    const empresaData = empresaSnap.val();

    if (!empresaData) {
      throw new functions.https.HttpsError("invalid-argument", "Empresa não encontrada.");
    }

    const telefoneNumerico = telefoneCliente.replace(/\D/g, "");

    const payment = await mercadopago.payment.create({
      transaction_amount: Number(valorTotal),
      description: `Pedido #${pedidoId} - ${nomeCliente}`,
      payment_method_id: "pix",
      payer: {
        email: `${pedidoId}@emailfake.com`,
        first_name: nomeCliente,
        identification: { type: "CPF", number: "12345678909" },
        address: {
          zip_code: "00000000",
          street_name: "Endereço",
          street_number: "SN",
          neighborhood: "Bairro",
          city: "Cidade",
          federal_unit: "UF"
        },
        phone: {
          area_code: telefoneNumerico.substring(0, 2),
          number: telefoneNumerico.substring(2)
        }
      },
      metadata: {
        empresaId: idEmpresa,
        pedidoId: pedidoId
      },
      notification_url: "https://us-central1-apkstella.cloudfunctions.net/notificacaoPagamentoPix"
    });

    return {
      qr_string: payment.body.point_of_interaction.transaction_data.qr_code,
      qr_base64: payment.body.point_of_interaction.transaction_data.qr_code_base64
    };
  } catch (error) {
    console.error("Erro ao gerar pagamento Pix:", error);
    throw new functions.https.HttpsError("internal", error.message || "Erro desconhecido");
  }
});



// ✅ Função 2 – Gerar link de pagamento com Pix (Checkout)
exports.gerarCheckoutPagamento = functions.https.onCall(async (data, context) => {
  try {
    const { idEmpresa, nomeCliente, telefoneCliente, enderecoEntrega, valorTotal, pedidoId } = data;
    const empresaRef = admin.database().ref(`empresa/${idEmpresa}`);
    const empresaSnapshot = await empresaRef.once("value");

    if (!empresaSnapshot.exists()) {
      throw new Error(`Empresa ${idEmpresa} não encontrada no banco de dados.`);
    }

    const empresaData = empresaSnapshot.val();
    const collector_id = empresaData?.mercadoPagoUserId;

    if (!collector_id) {
      console.error("❌ Campo mercadoPagoUserId ausente para empresa:", idEmpresa);
      throw new Error("ID do recebedor (mercadoPagoUserId) não encontrado.");
    }

    const valorTotalReais = parseFloat(valorTotal).toFixed(2);

    const preference = {
      payer: {
        name: nomeCliente,
        phone: {
          area_code: "11",
          number: 999999999
        }
      },
      items: [{
        title: `Pedido ${pedidoId} - ${nomeCliente}`,
        description: `Entrega: ${enderecoEntrega} | Tel: ${telefoneCliente}`,
        unit_price: parseFloat(valorTotalReais),
        quantity: 1
      }],
      payment_methods: {
        excluded_payment_types: [{ id: "ticket" }],
        default_payment_method_id: "pix"
      },
      back_urls: {
        success: "https://stelladitalia.web.app/sucesso",
        failure: "https://stelladitalia.web.app/cancelado",
        pending: "https://stelladitalia.web.app/pedido-pendente"
      },
      auto_return: "approved",
      notification_url: "https://us-central1-apkstella.cloudfunctions.net/notificacaoPagamento",
      metadata: { pedidoId, idEmpresa },
      marketplace_fee: parseFloat((valorTotal * 0.05).toFixed(2)),
      collector_id
    };

    const response = await mercadopago.preferences.create(preference);

    return {
      init_point: response.body.init_point,
      qr_base64: response.body.point_of_interaction?.transaction_data?.qr_code_base64,
      qr_string: response.body.point_of_interaction?.transaction_data?.qr_code
    };
  } catch (error) {
    console.error("❌ Erro ao criar pagamento MP:", error);
    throw new functions.https.HttpsError("internal", error.message);
  }
});

// ✅ Função 3 – Receber notificações do Mercado Pago
exports.notificacaoPagamento = functions.https.onRequest(async (req, res) => {
  try {
    const paymentId = req.query.id || req.body?.data?.id;

    if (!paymentId) return res.status(400).send("ID do pagamento não fornecido.");

    const pagamento = await mercadopago.payment.findById(paymentId);
    const status = pagamento.body.status;
    const metadata = pagamento.body.metadata;

    const pedidoId = metadata?.pedidoId;
    const idEmpresa = metadata?.idEmpresa;

    if (!pedidoId || !idEmpresa) {
      return res.status(400).send("Dados de metadata ausentes.");
    }

    await admin.database()
      .ref(`pedidos/${idEmpresa}/${pedidoId}`)
      .update({ statusPagamento: status });

    return res.status(200).send("Notificação processada com sucesso.");
  } catch (error) {
    console.error("Erro na notificação de pagamento:", error);
    return res.status(500).send("Erro interno");
  }
});

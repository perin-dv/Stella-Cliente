const functions = require("firebase-functions");
const admin = require("firebase-admin");
const mercadopago = require("mercadopago");
const cors = require("cors")({ origin: 'https://stella-d-italia.web.app' });

require("dotenv").config();
admin.initializeApp();

// 🔐 Configurar token do ambiente Firebase
mercadopago.configure({
  access_token: "TEST-168882420676566-061112-a83490ce1179b31acd3ef9d115f1db45-697565152"
});

// ✅ Função 1 – Gerar pagamento direto via Pix com QR Code
exports.criarPagamentoPix = functions.https.onCall(async (data, context) => {
  try {
    const {
      idEmpresa,
      nomeCliente = "Cliente",
      telefoneCliente = "",
      enderecoEntrega = {},
      valorTotal,
      pedidoId
    } = data;

    if (!idEmpresa || !valorTotal || !pedidoId) {
      throw new functions.https.HttpsError("invalid-argument", "Dados obrigatórios ausentes.");
    }

    // Busca a empresa no banco
    const empresaSnap = await admin.database().ref(`empresa/${idEmpresa}`).once("value");
    const empresaData = empresaSnap.val();

    if (!empresaData || !empresaData.access_token_conectado) {
      throw new functions.https.HttpsError("not-found", "Empresa não encontrada ou não conectada.");
    }

    const accessTokenEmpresa = empresaData.access_token_conectado;
    const userIdPlataforma = "168882420676566";

    // Cria client MercadoPago com o access_token da empresa
    const mp = new mercadopago.MercadoPagoConfig({ accessToken: accessTokenEmpresa });
    const paymentClient = new mercadopago.Payment(mp);

    const telefoneNumerico = (telefoneCliente || "").replace(/\D/g, "");
    const areaCode = telefoneNumerico.substring(0, 2) || "00";
    const number = telefoneNumerico.substring(2) || "000000000";

    const payment = await paymentClient.create({
      body: {
        transaction_amount: Number(valorTotal),
        description: `Pedido #${pedidoId} - ${nomeCliente}`,
        payment_method_id: "pix",
        payer: {
          email: `${pedidoId}@emailfake.com`,
          first_name: nomeCliente,
          identification: {
            type: "CPF",
            number: "12345678909"
          },
          address: {
            zip_code: "00000000",
            street_name: "Endereço",
            street_number: "SN",
            neighborhood: "Bairro",
            city: "Cidade",
            federal_unit: "UF"
          },
          phone: {
            area_code: areaCode,
            number: number
          }
        },
        metadata: {
          empresaId: idEmpresa,
          pedidoId: pedidoId
        },
        application_fee: Number(valorTotal) * 0.05, // 5% vai para você
        sponsor_id: userIdPlataforma,
        notification_url: "https://us-central1-apkstella.cloudfunctions.net/notificacaoPagamentoPix"
      }
    });

    const txData = payment.response.point_of_interaction.transaction_data;

    return {
      qr_string: txData.qr_code,
      qr_base64: txData.qr_code_base64,
      paymentId: payment.response.id
    };

  } catch (error) {
    console.error


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
  const collector_id = empresaData?.config?.mercadoPagoUserId || null;


    const valorTotalReais = parseFloat(valorTotal).toFixed(2);
    const preference = {
      payer: {
        name: nomeCliente,
         email: data.email || "teste@email.com",
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
       excluded_payment_types: [],
       installments: 1 ,
      },
      binary_mode: true,
      back_urls: {
        success: "https://stelladitalia.web.app/sucesso",
        failure: "https://stelladitalia.web.app/cancelado",
        pending: "https://stelladitalia.web.app/pedido-pendente"
      },
      auto_return: "approved",
     notification_url: "https://us-central1-stella-d-italia.cloudfunctions.net/notificacaoPagamento",
      metadata: { pedidoId, idEmpresa },
      marketplace_fee: parseFloat((valorTotal * 0.05).toFixed(2)),
       };

    const response = await mercadopago.preferences.create(preference);


    return {
      id: response.body.id,
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

exports.salvarCartao = functions.https.onRequest(async (req, res) => {
  const { token, userId } = req.body;

  const client = new mercadopago.MercadoPagoConfig({ accessToken: functions.config().mercadopago.token });

  // 1. Cria customer (uma vez só)
  const customer = await mercadopago.customer.create({
    body: {
      email: `${userId}@stella.com`,
      first_name: "Cliente",
      last_name: userId
    }
  }, { client });

  // 2. Associa cartão
  const savedCard = await mercadopago.card.create({
    body: {
      token: token,
      customer_id: customer.id
    }
  }, { client });

  // 3. Salva no Firebase
  await admin.firestore().collection("clientes").doc(userId).set({
    customer_id: customer.id,
    card_id: savedCard.id
  }, { merge: true });

  res.json({ customerId: customer.id, cardId: savedCard.id });
});
const fetch = require("node-fetch");


exports.concluirConexaoMP = functions.https.onRequest(async (req, res) => {
  const { code, state } = req.body;

  if (!code || !state) {
    return res.status(400).send({ erro: "Parâmetros ausentes." });
  }

  const body = {
    grant_type: "authorization_code",
    client_id: "168882420676566", // <-- seu Client ID
    client_secret: "NmVNb60Xx3iShEEcfSJfsX4IzxLy8m2x", // <-- seu Client Secret
    code,
    redirect_uri: "https://stella-d-italia.web.app/retorno-conexao.html"
  };

  try {
    const resposta = await fetch("https://api.mercadopago.com/oauth/token", {
      method: "POST",
      body: JSON.stringify(body),
      headers: { "Content-Type": "application/json" }
    });

    const dados = await resposta.json();

    if (dados.user_id && dados.access_token) {
      await admin.firestore()
        .collection("empresas")
        .doc(state) // empresaId passado no início
        .update({
          mp_access_token: dados.access_token,
          mp_user_id: dados.user_id,
          conectado: true
        });

      return res.status(200).send({ sucesso: true });
    } else {
      return res.status(400).send({ erro: "Erro ao obter token", detalhes: dados });
    }
  } catch (e) {
    return res.status(500).send({ erro: "Erro interno", detalhes: e.toString() });
  }
});

exports.process_payment = functions.https.onRequest((req, res) => {
  cors(req, res, async () => {
    try {
      const {
        token,
        issuer_id,
        transaction_amount,
        installments,
        payer,
        pedidoId,
        empresaId,
        clienteId
      } = req.body;

      const paymentData = {
        token,
        issuer_id: String(issuer_id || ""),
        transaction_amount: Number(transaction_amount) || 1,
        installments: Number(installments) || 1,
        payer: {
          email: payer?.email || "teste@email.com",
          identification: {
            type: payer?.identification?.type || "CPF",
            number: payer?.identification?.number || "00000000000"
          }
        },
        statement_descriptor: "STELLA ITALIA"
      };

      const payment = await mercadopago.payment.create(paymentData);
      const status = payment.body?.status || "erro";
      const paymentId = payment.body?.id || null;

      console.log("💰 Resultado do pagamento:", status, paymentId);

      if (status === "approved" && pedidoId && empresaId && clienteId) {
        // 🔄 Buscar pedido temporário salvo antes do pagamento
        const pedidoTempSnap = await admin.database()
          .ref(`pedidos_temporarios/${clienteId}/${pedidoId}`)
          .once("value");

        const pedidoTempData = pedidoTempSnap.val();

        if (pedidoTempData) {
          // 📝 Atualiza com status de pagamento
          pedidoTempData.status = "confirmado";
          pedidoTempData.statusPagamento = "approved";
          pedidoTempData.dataPagamento = Date.now();
          pedidoTempData.paymentId = paymentId;

          // 💾 Salva para a empresa
          await admin.database()
            .ref(`pedidos/${empresaId}/${pedidoId}`)
            .set(pedidoTempData);

          // 💾 Salva para o cliente
          await admin.database()
            .ref(`clientes/${clienteId}/pedidos/${pedidoId}`)
            .set(pedidoTempData);

          // 💾 Salva como pedido confirmado
          await admin.database()
            .ref(`pedidos_confirmados/${empresaId}/${pedidoId}`)
            .set(pedidoTempData);

          // 🧹 Remove temporário
          await admin.database()
            .ref(`pedidos_temporarios/${clienteId}/${pedidoId}`)
            .remove();

          console.log("✅ Pedido completo salvo após pagamento.");
        } else {
          console.warn("⚠️ Pedido temporário não encontrado.");
        }
      } else if (status !== "approved") {
        console.warn("⚠️ Pagamento recusado ou pendente. Pedido não salvo.");
      }

      res.status(200).send({
        status: status,
        id: paymentId,
        detail: payment.body
      });

    } catch (error) {
      console.error("❌ Erro ao processar pagamento:", error);
      res.status(500).send({ error: error.message });
    }
  });
});



exports.verificarStatusPagamento = functions.https.onCall(async (data, context) => {
  const paymentId = data.paymentId;

  if (!paymentId) {
    throw new functions.https.HttpsError("invalid-argument", "paymentId é obrigatório");
  }

  try {
    const pagamento = await mercadopago.payment.findById(paymentId);
    const status = pagamento.body.status;
    const statusDetail = pagamento.body.status_detail;

    console.log("📦 Status atual:", status, "| Detalhe:", statusDetail);

    return {
      status,
      statusDetail
    };
  } catch (error) {
    console.error("❌ Erro ao verificar pagamento:", error);
    throw new functions.https.HttpsError("internal", "Erro ao verificar status do pagamento.");
  }
});


exports.gerarPreferenceId = functions.https.onCall(async (data, context) => {
  const accessToken = "TEST-168882420676566-061112-a83490ce1179b31acd3ef9d115f1db45-697565152";

  const body = {
    items: [
      {
        title: data.titulo || "Pedido Stella",
        quantity: 1,
        currency_id: "BRL",
        unit_price: parseFloat(data.valor)
      }
    ],
    payer: {
      name: data.nomeCliente || "",
      email: data.email || "comprador_teste123@testuser.com"
    },
    back_urls: {
      success: "https://www.sualoja.com/sucesso",
      failure: "https://www.sualoja.com/falha"
    },
    auto_return: "approved"
  };

  try {
    const response = await axios.post("https://api.mercadopago.com/checkout/preferences", body, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json"
      }
    });

    return { preferenceId: response.data.id };
  } catch (error) {
    console.error("Erro ao gerar preference:", error);
    throw new functions.https.HttpsError("internal", "Erro ao gerar preference");
  }
});

package com.example.apkstelladitalia20.fragment

import PizzaTamanhoAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.activity.AdicionarPizzaTamanhoActivity
import com.example.apkstelladitalia20.activity.DetalhesProdutoActivity
import com.example.apkstelladitalia20.activity.DetalhesPromocaoActivity
import com.example.apkstelladitalia20.activity.HomeActivity
import com.example.apkstelladitalia20.adapter.CategoriaAdapter
import com.example.apkstelladitalia20.adapter.DestaquesAdapter
import com.example.apkstelladitalia20.adapter.PromocaoAdapter

import com.example.apkstelladitalia20.data.PizzaTamanho
import com.example.apkstelladitalia20.databinding.ActivityAdicionarPizzaTamanhoBinding
import com.example.apkstelladitalia20.databinding.FragmentHomeBinding
import com.example.apkstelladitalia20.helper.DepthPageTransformer
import com.example.apkstelladitalia20.model.CarrinhoViewModel
import com.example.apkstelladitalia20.model.PromocaoEntity
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.*
import com.stelladitalia.adapters.ProdutoAdapter


import java.util.*
import kotlin.jvm.java

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val handler = android.os.Handler()
    private lateinit var autoScrollRunnable: Runnable
    private var currentPage = 0
    private val destaques = mutableListOf<ProdutoEntity>()
    private var listaPromocoesDoBanner: List<PromocaoEntity> = emptyList()
    private lateinit var promocaoAdapter: PromocaoAdapter
    private lateinit var destaqueAdapter: DestaquesAdapter
    private lateinit var categoriaAdapter: CategoriaAdapter
    private lateinit var produtoAdapter: ProdutoAdapter
    private val produtosOrdenados = mutableListOf<ProdutoEntity>()
    private val carrinhoViewModel: CarrinhoViewModel by viewModels()

    private val prefs by lazy {
        requireContext().getSharedPreferences("appStella", Context.MODE_PRIVATE)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.coordinatorLayoutHome


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs.edit().putString("uidEmpresa", "7a3118oNdgcpmwSqrgyRTqBnFFx2").apply()



        carregarConfiguracoes()
        setupPizzaTamanhos()
        setupPizzaDoces()
        setupAdapters()
        carregarSaudacao()
        carregarEnderecoCliente()
        carregarProdutosOrdenados {
            carregarProdutosPorCategoria()
        }
        carregarPromocao()
        carregarDestaques()
        setupCategoryScroll()
        startAutoScroll()



    }


    override fun onResume() {
        super.onResume()

        binding.recyclerProdutos.adapter = null
        carregarProdutosPorCategoria()
    }


    private fun carregarProdutosOrdenados(onComplete: () -> Unit) {
        val empresaDb = FirebaseHelper.empresaDatabase(requireContext())
        val empresaKey = "7a3118oNdgcpmwSqrgyRTqBnFFx2"

        empresaDb.child("empresa").child(empresaKey).child("produtos")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listaTemp = mutableListOf<ProdutoEntity>()

                    for (produtoSnap in snapshot.children) {
                        val produto = produtoSnap.getValue(ProdutoEntity::class.java)
                        Log.d("CARREGAMENTO", "Produto lido: ${produto?.nome}")
                        if (produto != null && !produto.nome.isNullOrBlank()) {
                            listaTemp.add(produto)
                        }
                    }

                    produtosOrdenados.clear()
                    produtosOrdenados.addAll(listaTemp)

                    Log.d("CARREGAMENTO", "Produtos carregados: ${produtosOrdenados.size}")
                    onComplete()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomeFragment", "Erro ao carregar produtos ordenados: ${error.message}")
                }
            })
    }


    private fun setupAdapters() {
        binding.recyclerDestaques.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        destaqueAdapter = DestaquesAdapter(requireContext(), destaques) { }
        binding.recyclerDestaques.adapter = destaqueAdapter

        promocaoAdapter = PromocaoAdapter {
            val posAtual = binding.viewPagerPromocoes.currentItem
            val promocao = promocaoAdapter.currentList.getOrNull(posAtual)
            if (promocao != null) {
                val promocaoLimpa = promocao.copy(
                    produtos = promocao.produtos.map {
                        it.copy(imagem = "")
                    }
                )

                val intent = Intent(requireContext(), DetalhesPromocaoActivity::class.java)
                if (promocao.produtos.all { it.id.isNotBlank() && it.valor >= 0.0 }) {
                    intent.putExtra("promocaoSelecionada", promocaoLimpa)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Promoção incompleta ou com produtos inválidos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.recyclerProdutos.layoutManager =
            LinearLayoutManager(requireContext())
        categoriaAdapter = CategoriaAdapter(requireContext()) { produtoSelecionado ->
            val intent = Intent(context, DetalhesProdutoActivity::class.java)
            intent.putExtra("produtoId", produtoSelecionado.id)
            intent.putExtra("tipoProduto", produtoSelecionado.categoria?.lowercase() ?: "")
            startActivity(intent)

        }

        binding.recyclerProdutos.adapter = categoriaAdapter




        binding.viewPagerPromocoes.adapter = promocaoAdapter
        binding.viewPagerPromocoes.setPageTransformer(DepthPageTransformer())
        binding.tabLayoutIndicator.setSelectedTabIndicatorColor(Color.TRANSPARENT)
        binding.tabLayoutIndicator.setBackgroundColor(Color.TRANSPARENT)
        (binding.tabLayoutIndicator.parent as ViewGroup).clipChildren = false


        // Escala animada da bolinha selecionada
        binding.viewPagerPromocoes.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val tabStrip = binding.tabLayoutIndicator.getChildAt(0) as ViewGroup
                for (i in 0 until tabStrip.childCount) {
                    val tabView = tabStrip.getChildAt(i)
                    tabView.background = null
                    tabView.setBackgroundColor(android.graphics.Color.TRANSPARENT) // <- ESSENCIAL
                    tabView.animate()
                        .scaleX(if (i == position) 1.3f else 1f)
                        .scaleY(if (i == position) 1.3f else 1f)
                        .setDuration(300)
                        .setInterpolator(android.view.animation.OvershootInterpolator())
                        .start()
                }
            }
        })

        binding.etBusca.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.keyCode == android.view.KeyEvent.KEYCODE_ENTER)) {
                v.clearFocus() // remove foco do teclado

                val termo = v.text.toString().trim()
                if (termo.isNotEmpty()) {
                    v.isEnabled = false // desativa momentaneamente pra prevenir novo toque

                    carregarProdutosOrdenados {
                        Log.d("BUSCA", "Iniciando busca por: $termo")
                        exibirResultadoBusca(termo)
                        v.isEnabled = true // reativa depois que carregar
                    }
                }
                true
            } else {
                false
            }
        }





        binding.recyclerProdutos.layoutManager = LinearLayoutManager(requireContext())
        categoriaAdapter = CategoriaAdapter(requireContext()) { produtoSelecionado ->
            val intent = Intent(requireContext(), DetalhesProdutoActivity::class.java)
            intent.putExtra("produtoId", produtoSelecionado.id)
            startActivity(intent)
        }

        binding.recyclerProdutos.adapter = categoriaAdapter


        binding.viewPagerPromocoes.apply {
            offscreenPageLimit = 3
            (getChildAt(0) as RecyclerView).clipToPadding = false

            // Usa layout personalizado com a bolinha
            TabLayoutMediator(binding.tabLayoutIndicator, binding.viewPagerPromocoes) { tab, _ ->
                tab.setCustomView(R.layout.tab_custom_dot)
            }.attach()

            binding.tabLayoutIndicator.post {
                val tabStrip = binding.tabLayoutIndicator.getChildAt(0) as ViewGroup
                for (i in 0 until tabStrip.childCount) {
                    val tabView = tabStrip.getChildAt(i)

                    // 🔥 ESSENCIAL: forçar largura mínima da célula da aba
                    tabView.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                    tabView.requestLayout()

                    // 🔄 Garante que não haja fundo
                    tabView.background = null
                    tabView.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }
        binding.tabLayoutIndicator.post {
            val tabStrip = binding.tabLayoutIndicator.getChildAt(0) as ViewGroup
            for (i in 0 until tabStrip.childCount) {
                val tabView = tabStrip.getChildAt(i)

                // 🔥 Essencial: remover padding invisível aplicado pelo Material
                tabView.setPadding(0, 0, 0, 0)

                // Garante que o tabView use apenas o necessário
                tabView.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                tabView.background = null
                tabView.setBackgroundColor(Color.TRANSPARENT)
                tabView.requestLayout()
            }
        }

    }

    private fun exibirResultadoBusca(query: String) {
        val resultados = produtosOrdenados.filter {
            it.nome.contains(query, ignoreCase = true) ||
                    it.descricao?.contains(query, ignoreCase = true) == true
        }

        Log.d("BUSCA", "Resultado da busca: ${resultados.size}")

        if (resultados.isEmpty()) {
            Toast.makeText(requireContext(), "Nenhum resultado encontrado", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val bottomSheet = ResultadoBuscaBottomSheet.newInstance(resultados)
        bottomSheet.show(parentFragmentManager, "resultadoBusca")
    }

    private fun setupPizzaDoces() {
        val lista = listOf(
            PizzaTamanho("Pequena", "4 pedaços", "R$ 39,99", "https://firebasestorage.googleapis.com/pequena.jpg"),
            PizzaTamanho("Média", "6 pedaços", "R$ 41,99", "https://firebasestorage.googleapis.com/media.jpg"),
            PizzaTamanho("Grande", "8 pedaços", "R$ 44,99", "https://firebasestorage.googleapis.com/grande.jpg")
        )

        val adapter = PizzaTamanhoAdapter(lista) { tamanho ->
            val intent = Intent(requireContext(), AdicionarPizzaTamanhoActivity::class.java)
            intent.putExtra("tamanhoNome", tamanho.nome)
            intent.putExtra("precoBase", tamanho.preco)
            intent.putExtra("descricao", tamanho.descricao)
            intent.putExtra("imagemUrl", tamanho.imagem)
            intent.putExtra("tipo", "doce")
            startActivity(intent)
        }

        binding.recyclerPizzaTamanhosDoce.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerPizzaTamanhosDoce.adapter = adapter
    }



    private fun carregarProdutosPorCategoria() {
        val empresaKey = prefs.getString("uidEmpresa", "") ?: return
        val ref = FirebaseDatabase.getInstance()
            .getReference("empresa")
            .child(empresaKey)
            .child("produtos")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return
                val mapaCategorias = mutableMapOf<String, MutableList<ProdutoEntity>>()

                for (produtoSnap in snapshot.children) {
                    val raw = produtoSnap.value
                    if (raw is Map<*, *>) {
                        val produto = produtoSnap.getValue(ProdutoEntity::class.java)
                        if (produto != null && !produto.nome.isNullOrBlank()) {
                            val categoriaOriginal = produto.categoria?.trim()?.lowercase() ?: continue

                            // Ignora produtos genéricos que não devem aparecer no cardápio
                            val ignorar = listOf("água", "coca", "refrigerante")
                            if (ignorar.any { categoriaOriginal.contains(it) }) continue

                            val categoria = when {
                                categoriaOriginal.contains("doce") -> "Pizza Doce"
                                categoriaOriginal.contains("vegetariana") -> "Pizza Vegetariana"
                                categoriaOriginal.contains("premium") -> "Pizza Premium"
                                categoriaOriginal.contains("especial") -> "Pizza Especial"
                                categoriaOriginal.contains("tradicional") -> "Pizza Tradicional"
                                categoriaOriginal.contains("porção") -> "Porções"
                                categoriaOriginal.contains("sem álcool") -> "Bebidas sem álcool"
                                categoriaOriginal.contains("com álcool") -> "Bebidas com álcool"
                                categoriaOriginal.contains("bebida") -> "Bebidas"
                                else -> continue // <- evita que "Outros" apareça
                            }

                            Log.d("🔥 PRODUTO", "Carregado: ${produto.nome} | Categoria: $categoria")
                            mapaCategorias.getOrPut(categoria) { mutableListOf() }.add(produto)
                        } else {
                            Log.w("🔥 FirebaseParse", "Produto inválido em: ${produtoSnap.key}")
                        }
                    }
                }

                val ordemDesejada = listOf(
                    "Pizza Tradicional", "Pizza Especial", "Pizza Vegetariana",
                    "Pizza Premium", "Pizza Doce", "Porções",
                    "Bebidas sem álcool", "Bebidas com álcool", "Bebidas"
                )

                val listaOrdenada = mutableListOf<Pair<String, List<ProdutoEntity>>>()

                for (categoria in ordemDesejada) {
                    mapaCategorias[categoria]?.let {
                        listaOrdenada.add(categoria to it)
                    }
                }

                categoriaAdapter.atualizarLista(listaOrdenada)
                binding.recyclerProdutos.adapter = categoriaAdapter
                binding.recyclerProdutos.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Erro ao carregar categorias: ${error.message}")
            }
        })
      }


    fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.lowercase().replaceFirstChar(Char::titlecase) }

    private fun carregarSaudacao() {
        val nomeCache = prefs.getString("nome", null)
        binding.tvSaudacao.text = "Olá, ${nomeCache ?: "cliente"} 👋"

        val uidCliente = prefs.getString("uidCliente", "") ?: return
        val refCliente = FirebaseHelper.database.child("clientes").child(uidCliente)

        refCliente.child("nome").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nomeFirebase = snapshot.getValue(String::class.java)
                if (!nomeFirebase.isNullOrBlank() && nomeFirebase != nomeCache && isAdded) {
                    binding.tvSaudacao.text = "Olá, $nomeFirebase 👋"
                    prefs.edit().putString("nome", nomeFirebase).apply()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Erro ao buscar nome do cliente: ${error.message}")
            }
        })
    }

    private fun carregarEnderecoCliente() {
        val enderecoCache = prefs.getString("endereco", null)
        if (!enderecoCache.isNullOrBlank() && isAdded && _binding != null) {
            binding.tvEndereco.text = enderecoCache
        } else {
            binding.tvEndereco.text = "Carregando endereço..."
        }

        val uidCliente = prefs.getString("uidCliente", "") ?: return
        val refCliente = FirebaseHelper.database.child("clientes").child(uidCliente)

        refCliente.child("endereco").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rua = snapshot.child("rua").getValue(String::class.java) ?: ""
                if (rua.isNotEmpty() && rua != enderecoCache && isAdded && _binding != null) {
                    binding.tvEndereco.text = rua
                    prefs.edit().putString("endereco", rua).apply()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Erro ao buscar endereço: ${error.message}")
            }
        })
    }

    private fun startAutoScroll() {
        autoScrollRunnable = object : Runnable {
            override fun run() {
                val totalPages = promocaoAdapter.itemCount
                if (totalPages > 0) {
                    currentPage = (binding.viewPagerPromocoes.currentItem + 1) % totalPages
                    binding.viewPagerPromocoes.setCurrentItem(currentPage, true)
                    handler.postDelayed(this, 10000) // 10 segundos
                }
            }
        }
        handler.postDelayed(autoScrollRunnable, 10000)
    }

    private fun carregarPromocao() {
        val database = FirebaseDatabase.getInstance()
        val referencia = database
            .getReference("empresa")
            .child("7a3118oNdgcpmwSqrgyRTqBnFFx2")
            .child("promocoes")


        referencia.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaPromocoes = mutableListOf<PromocaoEntity>()

                for (dados in snapshot.children) {
                    try {
                        val promocaoMap = dados.value as? Map<String, Any> ?: continue

                        val produtosList = mutableListOf<ProdutoEntity>()
                        val produtosData = promocaoMap["produtos"] as? List<Any> ?: emptyList()

                        for (produtoData in produtosData) {
                            when (produtoData) {
                                is String -> {
                                    // Produto antigo (só ID)
                                    produtosList.add(ProdutoEntity(id = produtoData))
                                }

                                is Map<*, *> -> {
                                    // Produto completo (novo)
                                    produtosList.add(
                                        ProdutoEntity(
                                            id = produtoData["id"] as? String ?: "",
                                            nome = produtoData["nome"] as? String ?: "",
                                            imagem = produtoData["imagemBase64"] as? String
                                                ?: "",
                                            valor = (produtoData["valor"] as? Number)?.toDouble()
                                                ?: 0.0
                                        )
                                    )
                                }
                            }
                        }

                        val promocao = PromocaoEntity(
                            id = promocaoMap["id"] as? String ?: "",
                            idUsuario = promocaoMap["idUsuario"] as? String ?: "",
                            titulo = promocaoMap["titulo"] as? String ?: "",
                            observacao = promocaoMap["observacao"] as? String ?: "",
                            valor = (promocaoMap["valor"] as? Number)?.toDouble() ?: 0.0,
                            nome = promocaoMap["nome"] as? String ?: "",
                            descricao = promocaoMap["descricao"] as? String ?: "",
                            quantidade = (promocaoMap["quantidade"] as? Number)?.toInt() ?: 1,
                            imagemBase64 = promocaoMap["imagemBase64"] as? String ?: "",
                            produtos = produtosList
                        )

                        listaPromocoes.add(promocao)
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Erro ao converter promoção: ${e.message}")
                    }
                }

                if (listaPromocoes.isNotEmpty()) {
                    binding.viewPagerPromocoes.visibility = View.VISIBLE
                    binding.textSemPromocoes.visibility = View.GONE
                    listaPromocoesDoBanner = listaPromocoes
                    promocaoAdapter.submitList(null)
                    promocaoAdapter.submitList(listaPromocoes.toList())


                } else {
                    binding.viewPagerPromocoes.visibility = View.GONE
                    binding.textSemPromocoes.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Erro ao carregar promoções", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }


    private fun carregarDestaques() {
        val empresaDb = FirebaseHelper.empresaDatabase(requireContext())
        val empresaKey = prefs.getString("uidEmpresa", "") ?: ""

        empresaDb.child("empresa").child(empresaKey).child("produtos")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    destaques.clear()

                    for (produtoSnap in snapshot.children) {
                        val raw = produtoSnap.value
                        if (raw is Map<*, *>) {
                            val produto = produtoSnap.getValue(ProdutoEntity::class.java)
                            if (produto != null && !produto.nome.isNullOrBlank()) {
                                destaques.add(produto)
                            }
                        } else {
                            Log.w("🔥 Destaques", "Produto ignorado: ${produtoSnap.key}")
                        }
                    }

                    destaqueAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomeFragment", "Erro ao carregar destaques: ${error.message}")
                }
            })
    }


    private fun carregarConfiguracoes() {
        val empresaDb = FirebaseHelper.empresaDatabase(requireContext())
        val empresaKey = prefs.getString("uidEmpresa", "").orEmpty()

        if (empresaKey.isBlank()) {
            Log.e("CONFIG", "❌ UID empresa está vazio.")
            return
        }

        empresaDb.child("empresa").child(empresaKey).child("config")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val rawTaxa = snapshot.child("taxaEntrega").value
                    val rawTempo = snapshot.child("tempoEntrega").value

                    val taxaEntrega = when (rawTaxa) {
                        is Long -> rawTaxa.toDouble()
                        is Double -> rawTaxa
                        is String -> rawTaxa.toDoubleOrNull()
                        else -> null
                    }

                    val tempoEntrega = when (rawTempo) {
                        is Long -> rawTempo.toInt()
                        is Double -> rawTempo.toInt()
                        is String -> rawTempo.toIntOrNull()
                        else -> null
                    }

                    Log.d("CONFIG", "🧮 taxa=$taxaEntrega, tempo=$tempoEntrega")

                    if (taxaEntrega == null || tempoEntrega == null) {
                        Log.e("CONFIG", "❌ Dados inválidos ou mal formatados")
                        return
                    }

                    if (!isAdded || _binding == null) {
                        Log.w("CONFIG", "⚠️ Fragment destruído, não atualizando UI")
                        return
                    }

                    binding.root.post {
                        binding.txtTempoEntrega.text = "Entrega em até ${tempoEntrega} min"
                        binding.txtFrete.text = if (taxaEntrega == 0.0) {
                            binding.txtFrete.setTextColor(Color.parseColor("#2E7D32"))
                            "Frete Grátis!"
                        } else {
                            binding.txtFrete.setTextColor(Color.parseColor("#666666"))
                            "R$ %.2f".format(taxaEntrega)
                        }
                        binding.txtAvaliacao.text = "%.1f".format(4.8)

                        binding.txtTempoEntrega.visibility = View.VISIBLE
                        binding.txtFrete.visibility = View.VISIBLE
                        binding.txtAvaliacao.visibility = View.VISIBLE

                        Log.d("CONFIG", "✅ Configurações aplicadas")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CONFIG", "❌ Erro Firebase: ${error.message}")
                }
            })
    }

    private fun setupCategoryScroll() {
        binding.categoriaPizza.setOnClickListener {
            scrollToCategoria("Pizza Tradicional")
        }

        binding.categoriaMassas.setOnClickListener {
            scrollToCategoria("Pizza Premium")
        }

        binding.categoriaBebidas.setOnClickListener {
            scrollToCategoria("Bebidas sem álcool")
        }

        binding.categoriaSobremesas.setOnClickListener {
            scrollToCategoria("Pizza Doce")
        }

        binding.tvEndereco.setOnClickListener {
            abrirDialogEndereco()
        }
    }



    private fun setupPizzaTamanhos() {
        val lista = listOf(
            PizzaTamanho("Pequena", "4 pedaços", "R$ 39,99", "https://firebasestorage.googleapis.com/pequena.jpg"),
            PizzaTamanho("Média", "6 pedaços", "R$ 41,99", "https://firebasestorage.googleapis.com/media.jpg"),
            PizzaTamanho("Grande", "8 pedaços", "R$ 44,99", "https://firebasestorage.googleapis.com/grande.jpg")
        )

        val adapter = PizzaTamanhoAdapter(lista) { tamanho ->
            val intent = Intent(requireContext(), AdicionarPizzaTamanhoActivity::class.java)
            intent.putExtra("tamanhoNome", tamanho.nome)
            intent.putExtra("precoBase", tamanho.preco)
            intent.putExtra("descricao", tamanho.descricao)
            intent.putExtra("imagemUrl", tamanho.imagem)
            startActivity(intent)
        }

        binding.recyclerPizzaTamanhos.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerPizzaTamanhos.adapter = adapter
    }



    private fun solicitarPermissaoEConfigurarEndereco() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        } else {
            configurarEnderecoPorGps()
        }
    }

    private fun scrollToCategoria(nome: String) {
        val index = categoriaAdapter.getPosicaoCategoria(nome)
        if (index != -1) {
            binding.recyclerProdutos.smoothScrollToPosition(index)
        } else {
            Toast.makeText(requireContext(), "Categoria não encontrada", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun configurarEnderecoPorGps() {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(requireContext(), "Ative o GPS", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        } else {
            val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            location?.let {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val enderecoCurto = listOfNotNull(
                        address.thoroughfare,
                        address.subThoroughfare
                    ).joinToString(", ")
                    binding.tvEndereco.text = enderecoCurto
                    prefs.edit().putString("endereco", enderecoCurto).apply()
                }
            } ?: Toast.makeText(requireContext(), "Localização não encontrada", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun abrirDialogEndereco() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_endereco, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val btnGps = dialogView.findViewById<Button>(R.id.btnUsarGps)
        val edtManual = dialogView.findViewById<EditText>(R.id.edtEnderecoManual)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmarEndereco)

        btnGps.setOnClickListener {
            dialog.dismiss()
            solicitarPermissaoEConfigurarEndereco()
        }

        btnConfirmar.setOnClickListener {
            val enderecoDigitado = edtManual.text.toString().trim()
            if (enderecoDigitado.isNotEmpty()) {
                prefs.edit().putString("endereco", enderecoDigitado).apply()
                binding.tvEndereco.text = enderecoDigitado
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Digite um endereço válido", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(autoScrollRunnable)
        _binding = null
    }
}

package com.example.apkstelladitalia20.fragment

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
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.activity.DetalhesPromocaoActivity
import com.example.apkstelladitalia20.activity.PromocaoDetalhesActivity
import com.example.apkstelladitalia20.adapter.CategoriaAdapter
import com.example.apkstelladitalia20.adapter.DestaquesAdapter
import com.example.apkstelladitalia20.adapter.PromocaoAdapter
import com.example.apkstelladitalia20.databinding.FragmentHomeBinding
import com.example.apkstelladitalia20.helper.DepthPageTransformer
import com.example.apkstelladitalia20.helper.FirebaseHelper
import com.example.apkstelladitalia20.helper.ZoomOutPageTransformer
import com.example.apkstelladitalia20.model.PromocaoEntity
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.*
import com.stelladitalia.adapters.ProdutoAdapter

import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val handler = android.os.Handler()
    private lateinit var autoScrollRunnable: Runnable
    private var currentPage = 0
    private val destaques = mutableListOf<ProdutoEntity>()
    private val categorias = mutableListOf<Pair<String, List<ProdutoEntity>>>()
    private var listaPromocoesDoBanner: List<PromocaoEntity> = emptyList()
    private lateinit var promocaoAdapter: PromocaoAdapter
    private lateinit var destaqueAdapter: DestaquesAdapter
    private lateinit var categoriaAdapter: CategoriaAdapter
    private lateinit var produtoAdapter: ProdutoAdapter
    private val produtosOrdenados = mutableListOf<ProdutoEntity>()


    private val prefs by lazy {
        requireContext().getSharedPreferences("clientePrefs", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        prefs.edit().putString("uidEmpresa", "7a3118oNdgcpmwSqrgyRTqBnFFx2").apply()
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        carregarSaudacao()
        carregarEnderecoCliente()
        carregarProdutosOrdenados()
        carregarPromocao()
        carregarDestaques()
        carregarCategorias()
        carregarConfiguracoes()
        setupCategoryScroll()
        startAutoScroll()

    }
    private fun carregarProdutosOrdenados() {
        val empresaDb = FirebaseHelper.empresaDatabase(requireContext())
        val empresaKey = "7a3118oNdgcpmwSqrgyRTqBnFFx2"

        val ordemDesejada = listOf(
            "Pizza Tradicional",
            "Pizza Especial",
            "Pizza Vegetariana",
            "Pizza Premium",
            "Pizza Doce",
            "Porções",
            "Bebidas sem álcool",
            "Bebidas com álcool"
        )

        empresaDb.child("empresa").child(empresaKey).child("produtos")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listaTemp = mutableListOf<ProdutoEntity>()

                    for (categoria in ordemDesejada) {
                        val categoriaSnap = snapshot.child(categoria)
                        for (produtoSnap in categoriaSnap.children) {
                            produtoSnap.getValue(ProdutoEntity::class.java)?.let {
                                listaTemp.add(it)
                            }
                        }
                    }

                    produtosOrdenados.clear()
                    produtosOrdenados.addAll(listaTemp)
                    produtoAdapter.notifyDataSetChanged()
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
                    val intent = Intent(requireContext(), DetalhesPromocaoActivity::class.java)
                    intent.putExtra("promocaoSelecionada", promocao)
                    startActivity(intent)
                }
            }

            binding.recyclerProdutos.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            produtoAdapter = ProdutoAdapter(requireContext(), produtosOrdenados)
            { produtoSelecionado ->

        }
            binding.recyclerProdutos.adapter = produtoAdapter



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

            binding.rvCategorias.layoutManager = LinearLayoutManager(requireContext())
            categoriaAdapter = CategoriaAdapter(requireContext(), categorias) { }
            binding.rvCategorias.adapter = categoriaAdapter

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


    private fun carregarSaudacao() {
        val uidCliente = prefs.getString("uid", "") ?: return
        val refCliente = FirebaseHelper.database.child("clientes").child(uidCliente)

        refCliente.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nome = snapshot.child("nome").getValue(String::class.java)
                if (!nome.isNullOrBlank()) {
                    binding.tvSaudacao.text = "Olá, $nome 👋"
                    prefs.edit().putString("nome", nome).apply()
                } else {
                    binding.tvSaudacao.text = "Olá, cliente 👋"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Erro ao buscar nome do cliente: ${error.message}")
                binding.tvSaudacao.text = "Olá, cliente 👋"
            }
        })
    }
    private fun carregarEnderecoCliente() {
        val uidCliente = prefs.getString("uid", "") ?: return
        val refCliente = FirebaseHelper.database.child("clientes").child(uidCliente)

        refCliente.child("endereco").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val enderecoCadastro = snapshot.getValue(String::class.java)
                if (!enderecoCadastro.isNullOrEmpty()) {
                    if (isAdded && _binding != null) {
                        val enderecoFiltrado = enderecoCadastro.split("-")[0].trim()
                        binding.tvEndereco.text = enderecoFiltrado
                        prefs.edit().putString("endereco", enderecoCadastro).apply()
                    }
                } else {
                    if (isAdded) solicitarPermissaoEConfigurarEndereco()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Erro ao buscar endereço do cliente: ${error.message}")
                if (isAdded) solicitarPermissaoEConfigurarEndereco()
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
                                            imagemBase64 = produtoData["imagemBase64"] as? String
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
                    for (catSnap in snapshot.children) {
                        for (itemSnap in catSnap.children) {
                            itemSnap.getValue(ProdutoEntity::class.java)?.let { destaques.add(it) }
                        }
                    }
                    destaqueAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomeFragment", "Erro ao carregar destaques: ${error.message}")
                }
            })
    }

    private fun carregarCategorias() {
        val empresaDb = FirebaseHelper.empresaDatabase(requireContext())
        val empresaKey = prefs.getString("uidEmpresa", "") ?: ""

        empresaDb.child("empresa").child(empresaKey).child("produtos")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val produtosPorCategoria = mutableMapOf<String, MutableList<ProdutoEntity>>()

                    for (produtoSnap in snapshot.children) {
                        val produto = produtoSnap.getValue(ProdutoEntity::class.java) ?: continue
                        val categoria = produtoSnap.child("categoria").getValue(String::class.java) ?: "Sem categoria"

                        produtosPorCategoria.getOrPut(categoria) { mutableListOf() }.add(produto)
                    }

                    // Ordena manualmente na ordem desejada
                    val ordemDesejada = listOf(
                        "Pizza Tradicional",
                        "Pizza Especial",
                        "Pizza Vegetariana",
                        "Pizza Premium",
                        "Pizza Doce",
                        "Porções",
                        "Bebidas sem álcool",
                        "Bebidas com álcool"
                    )

                    categorias.clear()

                    // Adiciona na ordem correta
                    ordemDesejada.forEach { nome ->
                        produtosPorCategoria[nome]?.let { lista ->
                            categorias.add(Pair(nome, lista))
                        }
                    }

                    // Adiciona outras categorias não previstas (se houver)
                    produtosPorCategoria.forEach { (categoria, lista) ->
                        if (categoria !in ordemDesejada) {
                            categorias.add(Pair(categoria, lista))
                        }
                    }

                    categoriaAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomeFragment", "Erro ao carregar categorias: ${error.message}")
                }
            })
    }

    private fun carregarConfiguracoes() {
        val empresaDb = FirebaseHelper.empresaDatabase(requireContext())
        val empresaKey = "7a3118oNdgcpmwSqrgyRTqBnFFx2" // UID fixo da empresa

        Log.d("HomeFragment", "Buscando config da empresa: $empresaKey")

        empresaDb.child("empresa").child(empresaKey).child("config")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val taxaEntrega = snapshot.child("taxaEntrega").getValue(String::class.java)
                        val tempoEntrega = snapshot.child("tempoEntrega").getValue(String::class.java)

                        if (!taxaEntrega.isNullOrEmpty() && !tempoEntrega.isNullOrEmpty()) {
                            binding.txtTempoEntrega.text =
                                "Entrega em até $tempoEntrega min • R$ $taxaEntrega"
                        } else {
                            binding.txtTempoEntrega.text = "Informações indisponíveis"
                            Log.w("HomeFragment", "Dados incompletos: taxaEntrega ou tempoEntrega nulo")
                        }
                    } else {
                        Log.w("HomeFragment", "Snapshot não encontrado em /config")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomeFragment", "Erro ao carregar configurações: ${error.message}")
                }
            })
    }

    private fun setupCategoryScroll() {
        binding.categoriaPizza.setOnClickListener { scrollToView(binding.categoriaPizza) }
        binding.categoriaMassas.setOnClickListener { scrollToView(binding.categoriaMassas) }
        binding.categoriaBebidas.setOnClickListener { scrollToView(binding.categoriaBebidas) }
        binding.categoriaSobremesas.setOnClickListener { scrollToView(binding.categoriaSobremesas) }
        binding.tvEndereco.setOnClickListener { abrirDialogEndereco() }
    }

    private fun scrollToView(v: View) {
        binding.homeScroll.post { binding.homeScroll.smoothScrollTo(0, v.top) }
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

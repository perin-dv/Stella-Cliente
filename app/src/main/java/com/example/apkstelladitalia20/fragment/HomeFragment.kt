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
import android.text.Editable
import android.text.TextWatcher
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.activity.DetalhesProdutoActivity
import com.example.apkstelladitalia20.activity.DetalhesPromocaoActivity
import com.example.apkstelladitalia20.adapter.CategoriaAdapter
import com.example.apkstelladitalia20.adapter.DestaquesAdapter
import com.example.apkstelladitalia20.adapter.PromocaoAdapter
import com.example.apkstelladitalia20.databinding.FragmentHomeBinding
import com.example.apkstelladitalia20.helper.DepthPageTransformer
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
        requireContext().getSharedPreferences("appStella", Context.MODE_PRIVATE)
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
        carregarProdutosPorCategoria()
        carregarConfiguracoes()
        setupCategoryScroll()
        startAutoScroll()

    }

    private fun carregarProdutosOrdenados(onComplete: (() -> Unit)? = null) {
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
                    onComplete?.invoke()
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
            val intent = Intent(requireContext(), DetalhesProdutoActivity::class.java)
            intent.putExtra("produtoId", produtoSelecionado.id)
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


    private fun carregarProdutosPorCategoria() {
        val idUsuario = prefs.getString("uidEmpresa", "") ?: return
        val ref = FirebaseDatabase.getInstance()
            .getReference("empresa")
            .child(idUsuario)
            .child("produtos")

        ref.get().addOnSuccessListener { snapshot ->
            val mapaCategorias = mutableMapOf<String, MutableList<ProdutoEntity>>()

            for (produtoSnap in snapshot.children) {
                val produto = produtoSnap.getValue(ProdutoEntity::class.java)
                if (produto != null && !produto.nome.isNullOrBlank()) {
                    val categoria =
                        produto.categoria.takeIf { !it.isNullOrBlank() } ?: "Sem categoria"
                    mapaCategorias.getOrPut(categoria) { mutableListOf() }.add(produto)
                }
            }

            val ordemDesejada = listOf(
                "Pizza Tradicional",
                "Pizza Especial",
                "Pizza Vegetariana",
                "Pizza Premium",
                "Pizza Doce",
                "Porções",
                "Bebidas sem álcool",
                "Bebidas com álcool",
                "Sem categoria"
            )

            val listaOrdenada = mutableListOf<Pair<String, List<ProdutoEntity>>>()

            for (categoria in ordemDesejada) {
                mapaCategorias[categoria]?.let {
                    listaOrdenada.add(categoria to it)
                }
            }

            // adiciona qualquer categoria extra que não estava na ordem
            mapaCategorias.entries
                .filter { it.key !in ordemDesejada }
                .forEach { listaOrdenada.add(it.key to it.value) }

            categoriaAdapter.atualizarLista(listaOrdenada)
            binding.recyclerProdutos.adapter = categoriaAdapter
            binding.recyclerProdutos.visibility = View.VISIBLE
        }
    }


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
                    for (catSnap in snapshot.children) {
                        for (itemSnap in catSnap.children) {
                            val raw = itemSnap.value
                            if (raw is Map<*, *>) {
                                itemSnap.getValue(ProdutoEntity::class.java)?.let {
                                    destaques.add(it)
                                }
                            } else {
                                Log.w("HomeFragment", "Produto ignorado: não é objeto válido")
                            }
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
        val empresaKey = "7a3118oNdgcpmwSqrgyRTqBnFFx2"

        empresaDb.child("empresa").child(empresaKey)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val taxaStr = snapshot.child("taxaEntrega").value?.toString()
                    val tempoStr = snapshot.child("tempoEntrega").value?.toString()

                    val taxa = taxaStr?.toDoubleOrNull()
                    val tempo = tempoStr?.toIntOrNull()


                    if (taxa != null && tempo != null) {
                        binding.txtTempoEntrega.text = "Entrega em até ${tempo} min"

                        if (taxa == 0.0) {
                            binding.txtFrete.text = "Frete Grátis!"
                            binding.txtFrete.setTextColor(Color.parseColor("#2E7D32"))
                        } else {
                            binding.txtFrete.text = "R$ %.2f".format(taxa)
                            binding.txtFrete.setTextColor(Color.parseColor("#666666"))
                        }

                        binding.txtAvaliacao.text = "4.8"
                    } else {
                        binding.txtTempoEntrega.text = "Indisponível"
                        binding.txtFrete.text = ""
                        binding.txtAvaliacao.text = ""
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomeFragment", "Erro ao carregar configurações: ${error.message}")
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

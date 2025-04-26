package com.example.apkstelladitalia20.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.activity.PromocaoDetalhesActivity
import com.example.apkstelladitalia20.adapter.CategoriaAdapter
import com.example.apkstelladitalia20.adapter.DestaquesAdapter
import com.example.apkstelladitalia20.adapter.PromocaoAdapter
import com.example.apkstelladitalia20.databinding.FragmentHomeBinding
import com.example.apkstelladitalia20.helper.FirebaseHelper
import com.example.apkstelladitalia20.model.Promocao
import com.google.firebase.database.*

import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val promocoes = mutableListOf<Promocao>()
    private val destaques = mutableListOf<ProdutoEntity>()
    private val categorias = mutableListOf<Pair<String, List<ProdutoEntity>>>()

    private lateinit var promocaoAdapter: PromocaoAdapter
    private lateinit var destaqueAdapter: DestaquesAdapter
    private lateinit var categoriaAdapter: CategoriaAdapter

    private val prefs by lazy {
        requireContext().getSharedPreferences("clientePrefs", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        carregarSaudacao()
        carregarEnderecoCliente()
        carregarPromocoes()
        carregarDestaques()
        carregarCategorias()
        carregarConfiguracoes()
        setupCategoryScroll()
    }

    private fun setupAdapters() {
        binding.recyclerDestaques.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        destaqueAdapter = DestaquesAdapter(requireContext(), destaques) { }
        binding.recyclerDestaques.adapter = destaqueAdapter

        promocaoAdapter = PromocaoAdapter(promocoes) { promocao ->
            val intent = Intent(requireContext(), PromocaoDetalhesActivity::class.java)
            intent.putExtra("promocaoSelecionada", promocao)
            startActivity(intent)
        }
        binding.viewPagerPromocoes.adapter = promocaoAdapter

        binding.rvCategorias.layoutManager = LinearLayoutManager(requireContext())
        categoriaAdapter = CategoriaAdapter(requireContext(), categorias) { }
        binding.rvCategorias.adapter = categoriaAdapter

        binding.viewPagerPromocoes.apply {
            offscreenPageLimit = 3
            (getChildAt(0) as RecyclerView).clipToPadding = false
        }
    }

    private fun carregarSaudacao() {
        val nome = prefs.getString("nome", null)
        binding.tvSaudacao.text = if (!nome.isNullOrEmpty()) {
            "Olá, $nome 👋"
        } else "Olá, cliente 👋"
    }

    private fun carregarEnderecoCliente() {
        val uidCliente = prefs.getString("uid", "") ?: ""
        val refCliente = FirebaseHelper.clienteDatabase.child("clientes").child(uidCliente)

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
                Log.e("HomeFragment", "Erro ao buscar endereço do cadastro: ${error.message}")
                if (isAdded) solicitarPermissaoEConfigurarEndereco()
            }
        })
    }

    private fun carregarPromocoes() {
        val empresaDb = FirebaseHelper.empresaDatabase(requireContext())
        val empresaKey = prefs.getString("uidEmpresa", "") ?: ""

        empresaDb.child("empresa").child(empresaKey).child("promocoes")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    promocoes.clear()
                    snapshot.children.mapNotNullTo(promocoes) {
                        it.getValue(Promocao::class.java)
                    }
                    promocaoAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomeFragment", "Erro ao carregar promoções: ${error.message}")
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
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    categorias.clear()
                    for (catSnap in snapshot.children) {
                        val nomeCat = catSnap.key ?: "Sem categoria"
                        val lista = catSnap.children.mapNotNull {
                            it.getValue(ProdutoEntity::class.java)
                        }
                        categorias.add(Pair(nomeCat, lista))
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
        val empresaKey = prefs.getString("uidEmpresa", "") ?: ""

        empresaDb.child("empresa").child(empresaKey).child("config")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val taxaEntrega = snapshot.child("taxaEntrega").getValue(String::class.java)
                        val tempoEntrega = snapshot.child("tempoEntrega").getValue(String::class.java)
                        binding.txtTempoEntrega.text = "Entrega em até $tempoEntrega min • R$ $taxaEntrega"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomeFragment", "Erro ao carregar configuracoes: ${error.message}")
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
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
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
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
            } ?: Toast.makeText(requireContext(), "Localização não encontrada", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "Digite um endereço válido", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

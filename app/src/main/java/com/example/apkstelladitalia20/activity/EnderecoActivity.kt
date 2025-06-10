package com.example.apkstelladitalia20.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.adapter.EnderecoAdapter
import com.example.apkstelladitalia20.databinding.ActivityEnderecosBinding
import com.example.apkstelladitalia20.Entity.EnderecoEntity
import com.example.apkstelladitalia20.dialog.EnderecoEditarDialogFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class EnderecoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnderecosBinding
    private val listaEnderecos = mutableListOf<EnderecoEntity>()
    private lateinit var adapter: EnderecoAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnderecosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarEndereco)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarEndereco.setNavigationOnClickListener { finish() }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        adapter = EnderecoAdapter(
            listaEnderecos,
            onSelecionar = { endereco ->
                EnderecoBottomSheet(
                    endereco = endereco,
                    onEditar = {
                        Toast.makeText(this, "Editar: ${endereco.referencia}", Toast.LENGTH_SHORT).show()
                        // abrir bottomsheet de edição
                    },
                    onExcluido = {
                        listaEnderecos.remove(endereco)
                        adapter.atualizarLista(listaEnderecos)
                    }
                ).show(supportFragmentManager, "EnderecoBottomSheet")
            },
            onEditar = {},  // não é mais usado
            onDeletar = {}
        )

        binding.toolbarEndereco.setOnLongClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnLongClickListener true

            val enderecoTeste = EnderecoEntity(
                rua = "Rua Teste",
                numero = "123",
                bairro = "Centro",
                cidade = "Maringá",
                estado = "PR",
                cep = "87000000",
                referencia = "Em frente ao mercado"
            )

            FirebaseDatabase.getInstance()
                .getReference("clientes")
                .child(uid)
                .child("enderecos")
                .push()
                .setValue(enderecoTeste)
                .addOnSuccessListener {
                    Toast.makeText(this, "Endereço inserido no Firebase!", Toast.LENGTH_SHORT).show()
                    carregarEnderecosUsuario() // força o reload na hora
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao salvar endereço", Toast.LENGTH_SHORT).show()
                }

            true
        }


        binding.recyclerEnderecos.layoutManager = LinearLayoutManager(this)
        binding.recyclerEnderecos.adapter = adapter

        binding.searchEndereco.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarLista(newText)
                return true
            }
        })

        binding.btnLocalizacaoAtual.setOnClickListener {
            solicitarLocalizacao()
        }

        carregarEnderecosUsuario()
    }

    private fun solicitarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                Log.d("GPS_DEBUG", "Local: $location")

                if (location != null) {
                    val results = Geocoder(this, Locale.getDefault())
                        .getFromLocation(location.latitude, location.longitude, 1)

                    if (!results.isNullOrEmpty()) {
                        val enderecoFormatado = results[0]
                        val endereco = EnderecoEntity(
                            rua = enderecoFormatado.thoroughfare ?: "",
                            numero = enderecoFormatado.subThoroughfare ?: "",
                            bairro = enderecoFormatado.subLocality ?: "",
                            cidade = enderecoFormatado.locality ?: "",
                            estado = enderecoFormatado.adminArea ?: "",
                            cep = enderecoFormatado.postalCode ?: "",
                            referencia = "Localização Atual"
                        )

                        Toast.makeText(this, "Endereço localizado com sucesso!", Toast.LENGTH_SHORT).show()

                        EnderecoEditarDialogFragment(endereco) {
                            carregarEnderecosUsuario()
                        }.show(supportFragmentManager, "EditarEndereco")
                    }
                } else {
                    Toast.makeText(this, "Localização indisponível", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) solicitarLocalizacao()
            else Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show()
        }

    private fun carregarEnderecosUsuario() {
        val prefs = getSharedPreferences("appStella", Context.MODE_PRIVATE)
        val uid = prefs.getString("uidCliente", null)

        if (uid.isNullOrBlank()) {
            Toast.makeText(this, "Erro: cliente não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("clientes")
            .child(uid)
            .child("enderecos")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<EnderecoEntity>()

                for (child in snapshot.children) {
                    val endereco = child.getValue(EnderecoEntity::class.java)
                    endereco?.id = child.key
                    if (endereco != null) {
                        lista.add(endereco)
                    }
                }

                adapter.atualizarLista(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EnderecoActivity, "Erro ao buscar endereços", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun filtrarLista(query: String?) {
        val filtrada = if (query.isNullOrBlank()) listaEnderecos else listaEnderecos.filter {
            it.referencia.contains(query, true) ||
                    it.rua.contains(query, true) ||
                    it.bairro.contains(query, true)
        }
        adapter.atualizarLista(filtrada)
    }
}

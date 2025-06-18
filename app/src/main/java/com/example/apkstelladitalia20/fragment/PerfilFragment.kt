package com.example.apkstelladitalia20.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.activity.AjudaActivity
import com.example.apkstelladitalia20.activity.ConfiguracoesActivity
import com.example.apkstelladitalia20.activity.ConversasActivity
import com.example.apkstelladitalia20.activity.CuponsActivity
import com.example.apkstelladitalia20.activity.DadosContaActivity
import com.example.apkstelladitalia20.activity.EnderecoActivity
import com.example.apkstelladitalia20.activity.LeitorQrCodeActivity
import com.example.apkstelladitalia20.activity.SegurancaActivity
import com.example.apkstelladitalia20.adpter.OpcaoPerfilAdapter
import com.example.apkstelladitalia20.data.OpcaoPerfil
import com.example.apkstelladitalia20.databinding.FragmentPerfilBinding
import com.example.apkstelladitalia20.model.PerilViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("ViewBinding não inicializado")


    private val opcoes = listOf(
        OpcaoPerfil("Conversas", R.drawable.ic_conversas) { abrirConversas() },
        OpcaoPerfil("Dados da Conta", R.drawable.ic_dados_conta) { abrirConta() },
        OpcaoPerfil("Cupons", R.drawable.ic_cupons) { abrirCupons() },
        OpcaoPerfil("Endereços", R.drawable.ic_enderecos) { abrirEnderecos() },
        OpcaoPerfil("Ajuda", R.drawable.ic_ajuda) { abrirAjuda() },
        OpcaoPerfil("Configurações", R.drawable.ic_configuracoes) { abrirConfiguracoes() },
        OpcaoPerfil("Segurança", R.drawable.ic_seguranca) { abrirSeguranca() }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()


        carregarNomeUsuario()
        binding.btnQrCode.setOnClickListener {
            startActivity(Intent(requireContext(), LeitorQrCodeActivity::class.java))
        }
       }

    private fun setupRecyclerView() {
        binding.recyclerPerfil.adapter = OpcaoPerfilAdapter(opcoes)
    }

    private fun carregarNomeUsuario() {
        val prefs = requireContext().getSharedPreferences("appStella", AppCompatActivity.MODE_PRIVATE)
        val nomeCache = prefs.getString("nome", null)
        binding.txtNome.text = nomeCache ?: "Usuário"

        val uidCliente = prefs.getString("uidCliente", "") ?: return
        val refCliente = FirebaseDatabase.getInstance().getReference("clientes").child(uidCliente)

        refCliente.child("nome").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nomeFirebase = snapshot.getValue(String::class.java)
                if (!nomeFirebase.isNullOrBlank() && nomeFirebase != nomeCache && isAdded) {
                    binding.txtNome.text = nomeFirebase
                    prefs.edit().putString("nome", nomeFirebase).apply()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PerfilFragment", "Erro ao buscar nome do cliente: ${error.message}")
            }
        })
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun abrirConversas() {
        startActivity(Intent(requireContext(), ConversasActivity::class.java))
    }
    private fun abrirConta() {
        startActivity(Intent(requireContext(), DadosContaActivity::class.java))
    }
    private fun abrirCupons() {
        startActivity(Intent(requireContext(), CuponsActivity::class.java))
    }
    private fun abrirEnderecos() {
        startActivity(Intent(requireContext(), EnderecoActivity::class.java))
    }
    private fun abrirAjuda() {
        startActivity(Intent(requireContext(), AjudaActivity::class.java))
    }
    private fun abrirConfiguracoes() {
        startActivity(Intent(requireContext(), ConfiguracoesActivity::class.java))
    }
    private fun abrirSeguranca() {
        startActivity(Intent(requireContext(), SegurancaActivity::class.java))
    }

}

package com.example.apkstelladitalia20.ui.carrinho

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.model.ProdutoCarrinhoEntity
import com.example.apkstelladitalia20.activity.EnderecoEntregaActivity
import com.example.apkstelladitalia20.adapter.BebidaAdapter
import com.example.apkstelladitalia20.adapter.CarrinhoAdapter
import com.example.apkstelladitalia20.databinding.FragmentCarrinhoBinding
import com.example.apkstelladitalia20.model.BebidaEntity
import com.example.apkstelladitalia20.model.CarrinhoViewModel
import com.google.firebase.database.*

class CarrinhoFragment : Fragment() {

    private var _binding: FragmentCarrinhoBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CarrinhoViewModel
    private lateinit var produtoAdapter: CarrinhoAdapter
    private val listaCarrinho = mutableListOf<ProdutoCarrinhoEntity>()
    private var taxaEntregaFirebase: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarrinhoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[CarrinhoViewModel::class.java]

        setupToolbar()
        setupRecycler()
        setupBotaoContinuar()
        observarCarrinho()
        carregarTaxaEntregaFirebase()
        carregarBebidas()
    }

    private fun setupToolbar() {
        binding.includeToolbar.btnVoltar.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecycler() {
        produtoAdapter = CarrinhoAdapter(
            listaCarrinho,
            onResumoAtualizado = { atualizarResumo() },
            onRemoverItemBanco = { nome -> viewModel.removerPorNome(nome) },
            onAtualizarQuantidade = { item -> viewModel.adicionar(item) }
        )
        binding.recyclerItensCarrinho.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerItensCarrinho.adapter = produtoAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observarCarrinho() {
        viewModel.itensCarrinho.observe(viewLifecycleOwner) { lista ->
            listaCarrinho.clear()
            listaCarrinho.addAll(lista)

            val temItens = listaCarrinho.isNotEmpty()
            binding.layoutCarrinhoVazio.isVisible = !temItens
            binding.recyclerItensCarrinho.isVisible = temItens
            binding.txtSubtotal.isVisible = temItens
            binding.btnContinuar.isVisible = temItens

            binding.recyclerItensCarrinho.adapter?.notifyDataSetChanged()
            atualizarResumo()
        }
    }

    private fun atualizarResumo() {
        val subtotal = listaCarrinho.sumOf { it.valor * it.quantidade }
        val temItens = listaCarrinho.isNotEmpty()
        val taxaEntrega = if (temItens) taxaEntregaFirebase else 0.0
        val total = subtotal + taxaEntrega

        binding.txtSubtotal.text = "R$ %.2f".format(subtotal)
        binding.txtTaxaEntrega.text = if (temItens) "R$ %.2f".format(taxaEntrega) else "—"
        binding.txtTotal.text = "R$ %.2f".format(total)
        binding.btnContinuar.visibility = if (temItens) View.VISIBLE else View.GONE
    }

    private fun setupBotaoContinuar() {
        binding.btnContinuar.setOnClickListener {
            if (listaCarrinho.isNotEmpty()) {
                val intent = Intent(requireContext(), EnderecoEntregaActivity::class.java)
                intent.putExtra("carrinhoSelecionado", ArrayList(listaCarrinho))
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Adicione ao menos 1 item!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun carregarTaxaEntregaFirebase() {
        val uidEmpresa = "7a3118oNdgcpmwSqrgyRTqBnFFx2"
        val ref = FirebaseDatabase.getInstance()
            .getReference("empresa")
            .child(uidEmpresa)
            .child("config")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val taxa = snapshot.child("taxaEntrega").value?.toString()?.replace(",", ".")?.toDoubleOrNull()
                taxaEntregaFirebase = taxa ?: 0.0
                atualizarResumo()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun carregarBebidas() {
        val uidEmpresa = "7a3118oNdgcpmwSqrgyRTqBnFFx2"
        val ref = FirebaseDatabase.getInstance()
            .getReference("empresa")
            .child(uidEmpresa)
            .child("produtos")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return
                val listaBebidas = mutableListOf<BebidaEntity>()

                for (item in snapshot.children) {
                    val nome = item.child("nome").value?.toString() ?: continue
                    val preco = item.child("precoAtual").value?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                    val categoria = item.child("categoria").value?.toString() ?: ""
                    val imagem = item.child("imagem").value?.toString() ?: ""

                    if (categoria.contains("bebida", true)) {
                        listaBebidas.add(BebidaEntity(nome, preco, imagem))
                    }
                }

                val bebidaAdapter = BebidaAdapter(listaBebidas) { bebida ->
                    val item = ProdutoCarrinhoEntity(
                        idProduto = "bebida_${System.currentTimeMillis()}",
                        nome = bebida.nome,
                        valor = bebida.preco,
                        quantidade = 1,
                        tipo = "produto",
                        imagemUrl = bebida.imagem,
                        categoria = "bebida"
                    )
                    viewModel.adicionar(item)
                }

                binding.recyclerBebidas.apply {
                    layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    adapter = bebidaAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

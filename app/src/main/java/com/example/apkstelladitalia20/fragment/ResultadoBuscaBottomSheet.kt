package com.example.apkstelladitalia20.fragment

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkstelladitalia20.Entity.ProdutoEntity
import com.example.apkstelladitalia20.activity.DetalhesProdutoActivity
import com.example.apkstelladitalia20.databinding.BottomsheetResultadoBuscaBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.stelladitalia.adapters.ProdutoAdapter
import java.io.Serializable

class ResultadoBuscaBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: BottomsheetResultadoBuscaBinding
    private lateinit var adapter: ProdutoAdapter
    private var produtos: List<ProdutoEntity> = emptyList()

    // Callback opcional
    var onDismissCallback: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = BottomsheetResultadoBuscaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recupera produtos da Bundle
        @Suppress("UNCHECKED_CAST")
        produtos =
            arguments?.getSerializable("listaProdutos") as? List<ProdutoEntity> ?: emptyList()

        binding.tituloBusca.apply {
            text = "🔎 Resultados da sua busca"
            textSize = 18f
            setPadding(16, 16, 16, 8)
            setTextColor(resources.getColor(android.R.color.black, null))
        }

        adapter = ProdutoAdapter(requireContext(), produtos) { produtoSelecionado ->
            val intent = Intent(requireContext(), DetalhesProdutoActivity::class.java)
            intent.putExtra("produtoId", produtoSelecionado.id)
            startActivity(intent)
            dismiss()
        }

        binding.recyclerResultado.apply {
            layoutManager = LinearLayoutManager(requireContext()) // ← ESSENCIAL!
            adapter = this@ResultadoBuscaBottomSheet.adapter
            setPadding(12, 0, 12, 24)
            clipToPadding = false
        }

    }

        override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
    }

    companion object {
        fun newInstance(produtos: List<ProdutoEntity>): ResultadoBuscaBottomSheet {
            val fragment = ResultadoBuscaBottomSheet()
            val args = Bundle()
            args.putSerializable("listaProdutos", ArrayList(produtos as ArrayList<Serializable>))
            fragment.arguments = args
            return fragment
        }
    }
}

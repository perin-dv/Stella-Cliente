package com.example.apkstelladitalia20.ui.pedidos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.apkstelladitalia20.databinding.FragmentPedidosBinding
import com.example.apkstelladitalia20.model.PedidosViewModel

class PedidosFragment : Fragment() {

    private var _binding: FragmentPedidosBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(PedidosViewModel::class.java)

        _binding = FragmentPedidosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.tvTotalPedidoStatus
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
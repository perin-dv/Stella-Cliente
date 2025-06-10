package com.example.apkstelladitalia20.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.apkstelladitalia20.Entity.EnderecoEntity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.dialog.EnderecoEditarDialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EnderecoBottomSheet(
    private val endereco: EnderecoEntity,
    private val onEditar: (EnderecoEntity) -> Unit,
    private val onExcluido: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.layout_bottomsheet_endereco, container, false)

        view.findViewById<TextView>(R.id.txtTitulo).text = endereco.referencia
        view.findViewById<TextView>(R.id.txtEndereco).text =
            "${endereco.rua}, ${endereco.numero} - ${endereco.bairro}, ${endereco.cidade} - ${endereco.estado}"

        view.findViewById<MaterialButton>(R.id.btnEditar).setOnClickListener {
            EnderecoEditarDialogFragment(endereco) {
                // atualizar lista após edição
            }.show(parentFragmentManager, "EditarEndereco")
        }

        view.findViewById<MaterialButton>(R.id.btnExcluir).setOnClickListener {
            excluirEnderecoFirebase()
        }

        view.findViewById<MaterialButton>(R.id.btnCancelar).setOnClickListener {
            dismiss()
        }

        return view
    }

    private fun excluirEnderecoFirebase() {
        val prefs = context?.getSharedPreferences("appStella", android.content.Context.MODE_PRIVATE)
        val uidCliente = prefs?.getString("uidCliente", null)
        val idEndereco = endereco.id ?: return

        if (uidCliente == null) {
            Toast.makeText(context, "Erro ao identificar o usuário", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("clientes")
            .child(uidCliente)
            .child("enderecos")
            .child(idEndereco)

        ref.removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Endereço excluído", Toast.LENGTH_SHORT).show()
                dismiss()
                onExcluido()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao excluir", Toast.LENGTH_SHORT).show()
            }
    }
}

package com.example.apkstelladitalia20.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.apkstelladitalia20.Entity.EnderecoEntity
import com.example.apkstelladitalia20.databinding.DialogEditarEnderecoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EnderecoEditarDialogFragment(
    private val endereco: EnderecoEntity,
    private val onSalvo: () -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogEditarEnderecoBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogEditarEnderecoBinding.inflate(LayoutInflater.from(context))

        // Preenche os campos com o endereço atual
        binding.inputRua.setText(endereco.rua)
        binding.inputNumero.setText(endereco.numero)
        binding.inputBairro.setText(endereco.bairro)
        binding.inputCidade.setText(endereco.cidade)
        binding.inputEstado.setText(endereco.estado)
        binding.inputReferencia.setText(endereco.referencia)

        val builder = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setCancelable(true)

        binding.btnSalvar.setOnClickListener {
            Toast.makeText(requireContext(), "Botão clicado!", Toast.LENGTH_SHORT).show()
            salvarEndereco()
        }


        binding.btnCancelar.setOnClickListener {
            dismiss()
        }

        return builder.create()
    }

    private fun salvarEndereco() {
        val prefs = requireContext().getSharedPreferences("appStella", Context.MODE_PRIVATE)
        val uid = prefs.getString("uidCliente", null)

        if (uid.isNullOrBlank()) {
            Toast.makeText(context, "Erro: cliente não autenticado", Toast.LENGTH_SHORT).show()
            Log.e("SALVAR_ENDERECO", "UID de cliente nulo")
            return
        }

        val rua = binding.inputRua.text.toString()
        val numero = binding.inputNumero.text.toString()
        val bairro = binding.inputBairro.text.toString()
        val cidade = binding.inputCidade.text.toString()
        val estado = binding.inputEstado.text.toString()
        val referencia = binding.inputReferencia.text.toString()
        val cep = endereco.cep

        if (rua.isBlank() || numero.isBlank()) {
            Toast.makeText(context, "Preencha rua e número", Toast.LENGTH_SHORT).show()
            return
        }

        val enderecoAtualizado = EnderecoEntity(
            id = endereco.id,
            rua = rua,
            numero = numero,
            bairro = bairro,
            cidade = cidade,
            estado = estado,
            cep = cep,
            referencia = referencia
        )

        Log.d("SALVAR_ENDERECO", "Endereço: $enderecoAtualizado")

        val ref = FirebaseDatabase.getInstance()
            .getReference("clientes")
            .child(uid)
            .child("enderecos")

        if (endereco.id.isNullOrBlank()) {
            val newRef = ref.push()
            enderecoAtualizado.id = newRef.key

            newRef.setValue(enderecoAtualizado)
                .addOnSuccessListener {
                    Toast.makeText(context, "Endereço salvo com sucesso", Toast.LENGTH_SHORT).show()
                    onSalvo()
                    dismiss()
                }
                .addOnFailureListener {
                    Log.e("SALVAR_ENDERECO", "Erro ao salvar", it)
                    Toast.makeText(context, "Erro ao salvar", Toast.LENGTH_SHORT).show()
                }
        } else {
            ref.child(endereco.id!!).setValue(enderecoAtualizado)
                .addOnSuccessListener {
                    Toast.makeText(context, "Endereço atualizado com sucesso", Toast.LENGTH_SHORT).show()
                    onSalvo()
                    dismiss()
                }
                .addOnFailureListener {
                    Log.e("SALVAR_ENDERECO", "Erro ao atualizar", it)
                    Toast.makeText(context, "Erro ao atualizar", Toast.LENGTH_SHORT).show()
                }
        }
    }
}


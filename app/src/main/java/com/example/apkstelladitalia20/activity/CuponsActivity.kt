package com.example.apkstelladitalia20.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.adpter.CupomAdapter
import com.example.apkstelladitalia20.data.Cupom
import com.example.apkstelladitalia20.fragment.CupomBottomSheetFragment

class CuponsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cupons)

        val recycler = findViewById<RecyclerView>(R.id.recyclerCupons)

        val lista = listOf(
            Cupom("Clube: presente de R$ 22", "16:30 às 23:59 • Acima de R$ 50", "Acaba em 59min"),
            Cupom("Clube: presente de R$ 14", "16:30 às 23:59 • Acima de R$ 25", "Acaba em 59min"),
            Cupom("Kopenhagen: até R$10", "Até 23:59 • Acima de R$ 30", "Faltam 2h")
        )

        recycler.adapter = CupomAdapter(lista) { cupom ->
            val bottomSheet = CupomBottomSheetFragment.newInstance(cupom)
            bottomSheet.show(supportFragmentManager, "CupomBottomSheet")
        }
        recycler.layoutManager = LinearLayoutManager(this)
    }
}

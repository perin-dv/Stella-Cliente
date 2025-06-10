package com.example.apkstelladitalia20.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.databinding.ActivityEntregaTrackingBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class EntregaTrackingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityEntregaTrackingBinding
    private lateinit var map: GoogleMap
    private lateinit var databaseRef: DatabaseReference
    private var marcadorMotoboy: Marker? = null
    private lateinit var idPedido: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntregaTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idPedido = intent.getStringExtra("idPedido") ?: return finish()

        val mapFragment = supportFragmentManager
            .findFragmentById(binding.mapContainer.id) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        escutarLocalizacaoMotoboy()
    }

    private fun escutarLocalizacaoMotoboy() {
        databaseRef = FirebaseDatabase.getInstance()
            .getReference("localizacoesEntregadores")
            .child(idPedido)

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lat = snapshot.child("latitude").getValue(Double::class.java)
                val lng = snapshot.child("longitude").getValue(Double::class.java)

                if (lat != null && lng != null) {
                    val posicao = LatLng(lat, lng)
                    if (marcadorMotoboy == null) {
                        marcadorMotoboy = map.addMarker(
                            MarkerOptions().position(posicao).title("Motoboy")
                        )
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(posicao, 16f))
                    } else {
                        marcadorMotoboy?.position = posicao
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EntregaTrackingActivity, "Erro: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

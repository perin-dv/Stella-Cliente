package com.example.apkstelladitalia20.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.fragment.HomeFragment
import com.example.apkstelladitalia20.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView



class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)



        onNewIntent(intent)



        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_home) as NavHostFragment




        val navController = navHostFragment.navController
        binding.navView.setupWithNavController(navController)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // muito importante: atualiza o intent ativo

        val abrirCarrinho = intent?.getBooleanExtra("abrirCarrinho", false) ?: false
        val abrirHome = intent?.getBooleanExtra("abrirHome", false) ?: false
        val abrirPedidos = intent?.getBooleanExtra("abrir_pedidos", false) ?: false

        Handler(Looper.getMainLooper()).post {
            when {
                abrirCarrinho -> binding.navView.selectedItemId = R.id.navigation_carrinho
                abrirHome -> binding.navView.selectedItemId = R.id.navigation_home
                abrirPedidos -> binding.navView.selectedItemId = R.id.navigation_pedidos
            }
        }
    }


    override fun onResume() {
        super.onResume()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_home) as? NavHostFragment
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()
        if (currentFragment is HomeFragment) {
            currentFragment.onResume()
        }
        if (intent.getBooleanExtra("abrirPedidos", false)) {
            binding.navView.selectedItemId = R.id.navigation_pedidos
            intent.removeExtra("abrirPedidos")
        }
    }


}

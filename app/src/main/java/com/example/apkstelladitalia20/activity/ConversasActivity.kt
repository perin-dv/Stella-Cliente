package com.example.apkstelladitalia20.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.adpter.ConversasPagerAdapter
import com.example.apkstelladitalia20.databinding.ActivityConversasBinding
import com.google.android.material.tabs.TabLayoutMediator

class ConversasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConversasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura Toolbar
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configura ViewPager
        val pagerAdapter = ConversasPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
        tab.text = if (pos == 0) "Recentes" else "Finalizadas"
        }.attach()
    }
}

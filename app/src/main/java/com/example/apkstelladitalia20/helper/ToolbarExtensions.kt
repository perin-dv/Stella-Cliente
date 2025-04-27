package com.example.apkstelladitalia20.helper

import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.databinding.ToolbarLayoutBinding

fun AppCompatActivity.setupToolbar(binding: ToolbarLayoutBinding) {
    binding.btnVoltar.setOnClickListener {
        finish()
    }
}

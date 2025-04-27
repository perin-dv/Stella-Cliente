package com.example.apkstelladitalia20.helper

import android.view.View
import androidx.viewpager2.widget.ViewPager2

class ZoomOutPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            val pageHeight = height

            when {
                position < -1 -> { // Página fora da esquerda
                    alpha = 0f
                }
                position <= 1 -> { // Página visível
                    val scaleFactor = Math.max(0.85f, 1 - Math.abs(position))
                    val vertMargin = pageHeight * (1 - scaleFactor) / 2
                    val horzMargin = pageWidth * (1 - scaleFactor) / 2
                    translationX = if (position < 0) {
                        horzMargin - vertMargin / 2
                    } else {
                        -horzMargin + vertMargin / 2
                    }

                    scaleX = scaleFactor
                    scaleY = scaleFactor

                    alpha = (0.5f + (scaleFactor - 0.85f) / (1 - 0.85f) * (1 - 0.5f))
                }
                else -> { // Página fora da direita
                    alpha = 0f
                }
            }
        }
    }
}

package com.example.apkstelladitalia20.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apkstelladitalia20.R
import com.example.apkstelladitalia20.databinding.ActivityLeitorQrCodeBinding
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class LeitorQrCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeitorQrCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeitorQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.scannerView.decodeContinuous(callback)

        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }

    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            result?.text?.let { conteudo ->
                binding.scannerView.pause()
                Toast.makeText(this@LeitorQrCodeActivity, "Código lido: $conteudo", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
    }

    override fun onResume() {
        super.onResume()
        binding.scannerView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.scannerView.pause()
    }
}


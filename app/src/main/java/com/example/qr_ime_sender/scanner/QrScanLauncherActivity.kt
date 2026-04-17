package com.example.qr_ime_sender.scanner

import android.app.Activity
import android.os.Bundle
import com.example.qr_ime_sender.store.PendingScanStore
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

class QrScanLauncherActivity : Activity() {

    private var launched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        if (launched) return
        launched = true

        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_DATA_MATRIX,
                Barcode.FORMAT_AZTEC,
                Barcode.FORMAT_PDF417
            )
            .enableAutoZoom()
            .build()

        val scanner = GmsBarcodeScanning.getClient(this, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val qrData = barcode.rawValue?.trim().orEmpty()
                if (qrData.isNotEmpty()) {
                    PendingScanStore.save(this, qrData)
                }
                finish()
            }
            .addOnCanceledListener {
                finish()
            }
            .addOnFailureListener {
                finish()
            }
    }
}
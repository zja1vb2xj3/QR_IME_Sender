package com.contrastcre.qr_ime_sender.scanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.contrastcre.qr_ime_sender.MainActivity
import com.contrastcre.qr_ime_sender.store.PendingScanStore
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

class QrScanLauncherActivity : Activity() {

    private var launched = false
    private val maxRetryCount = 3
    private var retryCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        if (launched) return
        launched = true
        startQrScan()
    }

    private fun startQrScan() {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_DATA_MATRIX
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

                closeScannerTask()
            }
            .addOnCanceledListener {
                moveToMainAndRemoveScannerTask()
            }
            .addOnFailureListener {
                retryCount++

                if (retryCount <= maxRetryCount) {
                    Toast.makeText(
                        this,
                        "스캔에 실패했습니다. 다시 시도합니다. ($retryCount/$maxRetryCount)",
                        Toast.LENGTH_SHORT
                    ).show()

                    window.decorView.postDelayed({
                        startQrScan()
                    }, 500)
                } else {
                    Toast.makeText(
                        this,
                        "스캔에 계속 실패했습니다. 초기 화면으로 이동합니다.",
                        Toast.LENGTH_SHORT
                    ).show()

                    moveToMainAndRemoveScannerTask()
                }
            }
    }

    private fun closeScannerTask() {
        finishAndRemoveTask()
    }

    private fun moveToMainAndRemoveScannerTask() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        startActivity(intent)
        finishAndRemoveTask()
    }
}
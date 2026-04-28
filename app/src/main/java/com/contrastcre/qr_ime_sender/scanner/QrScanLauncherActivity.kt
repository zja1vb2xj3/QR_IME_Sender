package com.contrastcre.qr_ime_sender.scanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.contrastcre.qr_ime_sender.MainActivity
import com.contrastcre.qr_ime_sender.R
import com.contrastcre.qr_ime_sender.store.PendingScanStore
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class QrScanLauncherActivity : Activity() {

    private var launched = false
    private var retryCount = 0

    private val maxRetryCount: Int by lazy {
        resources.getInteger(R.integer.max_retry_count)
    }

    private val retryDelayMs: Long by lazy {
        resources.getInteger(R.integer.scan_retry_delay_ms).toLong()
    }

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
                        getString(
                            R.string.scan_failed_retry,
                            retryCount,
                            maxRetryCount
                        ),
                        Toast.LENGTH_SHORT
                    ).show()

                    window.decorView.postDelayed({
                        startQrScan()
                    }, retryDelayMs)
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.scan_failed_go_main),
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
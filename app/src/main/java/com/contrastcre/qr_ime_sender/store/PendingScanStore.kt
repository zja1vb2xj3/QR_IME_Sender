package com.contrastcre.qr_ime_sender.store

import android.content.Context
import androidx.core.content.edit

object PendingScanStore {

    private const val PREFS_NAME = "pending_scan_store"
    private const val KEY_PENDING_QR = "pending_qr"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(context: Context, value: String) {
        prefs(context).edit {
            putString(KEY_PENDING_QR, value)
        }
    }

    fun get(context: Context): String? {
        return prefs(context).getString(KEY_PENDING_QR, null)
    }

    fun clear(context: Context) {
        prefs(context).edit {
            remove(KEY_PENDING_QR)
        }
    }
}
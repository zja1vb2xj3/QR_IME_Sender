package com.example.qr_ime_sender.store

import android.content.Context

object PendingScanStore {
    private const val PREFS_NAME = "qr_ime_sender_prefs"
    private const val KEY_PENDING_QR = "pending_qr"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(context: Context, value: String) {
        prefs(context).edit().putString(KEY_PENDING_QR, value).apply()
    }

    fun get(context: Context): String? {
        return prefs(context).getString(KEY_PENDING_QR, null)
    }

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_PENDING_QR).apply()
    }
}
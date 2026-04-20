package com.example.qr_ime_sender.ime

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.widget.Button
import com.example.qr_ime_sender.R
import com.example.qr_ime_sender.scanner.QrScanLauncherActivity
import com.example.qr_ime_sender.store.PendingScanStore

class QrImeService : InputMethodService() {

    private val autoEnterAfterScan = true

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.ime_input_view, null)

        val btnScanQr = view.findViewById<Button>(R.id.btnScanQr)
        val btnClear = view.findViewById<Button>(R.id.btnClear)
        val btnKeyboardSettings = view.findViewById<Button>(R.id.btnKeyboardSettings)

        btnScanQr.setOnClickListener {
            openScanner()
        }

        btnClear.setOnClickListener {
            clearCurrentInput()
        }

        btnKeyboardSettings.setOnClickListener {
            openKeyboardSettings()
        }

        return view
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        flushPendingScanIfAny()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        flushPendingScanIfAny()
    }

    override fun onWindowShown() {
        super.onWindowShown()
        flushPendingScanIfAny()
    }

    private fun openScanner() {
        val intent = Intent(this, QrScanLauncherActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        startActivity(intent)
    }

    private fun openKeyboardSettings() {
        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun flushPendingScanIfAny() {
        val pending = PendingScanStore.get(this) ?: return
        val ic = currentInputConnection ?: return
        val editorInfo = currentInputEditorInfo
        val targetPackage = editorInfo?.packageName

        if (targetPackage == packageName) {
            return
        }

        ic.beginBatchEdit()
        try {
            ic.commitText(pending, 1)
        } finally {
            ic.endBatchEdit()
        }

        if (autoEnterAfterScan) {
            sendEnterKey()
        }

        PendingScanStore.clear(this)
    }

    private fun clearCurrentInput() {
        val ic = currentInputConnection ?: return
        val editorInfo = currentInputEditorInfo
        val targetPackage = editorInfo?.packageName

        if (targetPackage == packageName) {
            return
        }

        ic.beginBatchEdit()
        try {
            val extracted = ic.getExtractedText(ExtractedTextRequest(), 0)
            val fullText = extracted?.text?.toString()

            if (!fullText.isNullOrEmpty()) {
                ic.setSelection(0, fullText.length)
                ic.commitText("", 1)
                return
            }

            val before = ic.getTextBeforeCursor(10000, 0)?.length ?: 0
            val after = ic.getTextAfterCursor(10000, 0)?.length ?: 0

            if (before > 0 || after > 0) {
                ic.deleteSurroundingText(before, after)
            }
        } finally {
            ic.endBatchEdit()
        }
    }

    private fun sendEnterKey() {
        val ic = currentInputConnection ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }
}
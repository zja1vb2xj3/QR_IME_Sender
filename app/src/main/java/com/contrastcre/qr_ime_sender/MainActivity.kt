package com.contrastcre.qr_ime_sender

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private var isImeEnabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isImeEnabled = isQrImeSenderEnabled()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        isImeEnabled = isImeEnabled,
                        onOpenImeSettings = {
                            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                        },
                        onSelectIme = {
                            showInputMethodPicker()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isImeEnabled = isQrImeSenderEnabled()
    }
}

@Composable
fun MainScreen(
    isImeEnabled: Boolean,
    onOpenImeSettings: () -> Unit,
    onSelectIme: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.main_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.usage_text),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isImeEnabled) {
                stringResource(R.string.ime_status_enabled)
            } else {
                stringResource(R.string.ime_status_disabled)
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onOpenImeSettings
        ) {
            Text(stringResource(R.string.step1_enable_ime))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            enabled = isImeEnabled,
            onClick = onSelectIme
        ) {
            Text(stringResource(R.string.step2_select_ime))
        }
    }
}

private fun Context.isQrImeSenderEnabled(): Boolean {
    val imm = getSystemService(InputMethodManager::class.java)

    return imm.enabledInputMethodList.any { inputMethodInfo ->
        inputMethodInfo.packageName == packageName
    }
}

private fun Context.showInputMethodPicker() {
    val imm = getSystemService(InputMethodManager::class.java)
    imm.showInputMethodPicker()
}
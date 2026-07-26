package com.expenseai.manager.presentation.auth

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.expenseai.manager.presentation.settings.SettingsViewModel
import com.expenseai.manager.ui.theme.*
import com.expenseai.manager.util.BiometricHelper

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsState()
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showPinInput by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (prefs.isBiometricEnabled) {
            val activity = context as? FragmentActivity ?: return@LaunchedEffect
            if (BiometricHelper.isBiometricAvailable(activity)) {
                BiometricHelper.authenticate(
                    activity = activity,
                    title = "Authenticate",
                    subtitle = "Verify your identity to access Expense AI Manager",
                    onSuccess = { onAuthSuccess() },
                    onError = { showPinInput = prefs.isPinEnabled },
                    onFailed = {}
                )
            } else {
                showPinInput = prefs.isPinEnabled
            }
        } else if (prefs.isPinEnabled) {
            showPinInput = true
        } else {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Blue30, Blue10))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.AccountBalance,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Expense AI Manager",
                style = MaterialTheme.typography.headlineSmall,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Your Smart Finance Companion",
                style = MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(48.dp))

            if (showPinInput) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Enter PIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { if (it.length <= 6) pin = it.filter { c -> c.isDigit() } },
                            label = { Text("4-6 digit PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            isError = pinError,
                            supportingText = if (pinError) { { Text("Invalid PIN. Try again.") } } else null,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (BiometricHelper.verifyPin(pin, prefs.pinHash)) {
                                    onAuthSuccess()
                                } else {
                                    pinError = true
                                    pin = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Unlock")
                        }

                        if (prefs.isBiometricEnabled) {
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = {
                                val activity = context as? FragmentActivity ?: return@TextButton
                                BiometricHelper.authenticate(
                                    activity, "Authenticate", "Use biometric to unlock",
                                    onSuccess = { onAuthSuccess() },
                                    onError = {}, onFailed = {}
                                )
                            }) {
                                Icon(Icons.Default.Fingerprint, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Use Biometric")
                            }
                        }
                    }
                }
            } else {
                CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White)
            }
        }
    }
}

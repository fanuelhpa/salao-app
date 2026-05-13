package com.salao.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salao.app.data.BiometricHelper
import com.salao.app.data.SessionManager
import com.salao.app.data.TokenState
import com.salao.app.ui.screens.HomeScreen
import com.salao.app.ui.screens.LoginScreen
import com.salao.app.ui.theme.*
import com.salao.app.viewmodel.AuthViewModel

class MainActivity : FragmentActivity() {

    val biometriaAutenticada = mutableStateOf(false)
    val biometriaCancelada = mutableStateOf(false)
    private var telefoneBloqueado = false

    private val bloqueioReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    telefoneBloqueado = true
                }
                Intent.ACTION_USER_PRESENT -> {
                    // Não faz nada aqui — deixa o onResume tratar
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.init(this)
        enableEdgeToEdge()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(bloqueioReceiver, filter)

        setContent {
            SalaoAppTheme {
                SalaoApp(activity = this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Quando o app volta ao primeiro plano, verifica se o telefone foi bloqueado
        if (telefoneBloqueado) {
            telefoneBloqueado = false
            // Atualiza na main thread — garante que o Compose redesenha
            runOnUiThread {
                biometriaAutenticada.value = false
                biometriaCancelada.value = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(bloqueioReceiver)
        } catch (e: Exception) {}
    }
}

@Composable
fun SalaoApp(activity: FragmentActivity) {
    val authViewModel: AuthViewModel = viewModel()

    val tokenState by authViewModel.tokenState
        .collectAsStateWithLifecycle(initialValue = TokenState.Carregando)

    val mainActivity = activity as MainActivity
    var biometriaAutenticada by mainActivity.biometriaAutenticada
    var biometriaCancelada by mainActivity.biometriaCancelada

    when (tokenState) {
        is TokenState.Carregando -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VerdeSurface),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = VerdeMusgo)
            }
        }

        is TokenState.ComToken -> {
            when {
                biometriaAutenticada -> {
                    HomeScreen(
                        token = (tokenState as TokenState.ComToken).token,
                        onLogout = { authViewModel.logout() }
                    )
                }
                biometriaCancelada -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(VerdeSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "✂", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Salao de Beleza",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerdeMusgo
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = {
                                    biometriaCancelada = false
                                    BiometricHelper.autenticar(
                                        activity = activity,
                                        onSucesso = { biometriaAutenticada = true },
                                        onFalha = { biometriaCancelada = true }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VerdeMusgo
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Entrar no app", color = Branco)
                            }
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(VerdeSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = VerdeMusgo)
                    }

                    LaunchedEffect(biometriaAutenticada) {
                        if (BiometricHelper.isBiometricAvailable(activity)) {
                            BiometricHelper.autenticar(
                                activity = activity,
                                onSucesso = { biometriaAutenticada = true },
                                onFalha = { biometriaCancelada = true }
                            )
                        } else {
                            biometriaAutenticada = true
                        }
                    }
                }
            }
        }

        is TokenState.SemToken -> {
            LaunchedEffect(Unit) {
                biometriaAutenticada = false
                biometriaCancelada = false
            }
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { biometriaAutenticada = true }
            )
        }
    }
}
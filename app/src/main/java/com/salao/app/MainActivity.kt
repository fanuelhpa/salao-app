package com.salao.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salao.app.data.TokenState
import com.salao.app.ui.screens.HomeScreen
import com.salao.app.ui.screens.LoginScreen
import com.salao.app.ui.theme.SalaoAppTheme
import com.salao.app.ui.theme.VerdeSurface
import com.salao.app.ui.theme.VerdeMusgo
import com.salao.app.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalaoAppTheme {
                SalaoApp()
            }
        }
    }
}

@Composable
fun SalaoApp() {
    val authViewModel: AuthViewModel = viewModel()

    // Carregando como estado inicial — antes do DataStore responder
    val tokenState by authViewModel.tokenState
        .collectAsStateWithLifecycle(initialValue = TokenState.Carregando)

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
            HomeScreen(
                token = (tokenState as TokenState.ComToken).token,
                onLogout = { authViewModel.logout() }
            )
        }
        is TokenState.SemToken -> {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { }
            )
        }
    }
}
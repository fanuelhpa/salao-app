package com.salao.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.salao.app.ui.theme.*
import com.salao.app.viewmodel.AgendamentoViewModel
import com.salao.app.viewmodel.ClienteViewModel

// Enum define as abas disponíveis na tela home
// Cada aba tem um rótulo que aparece na barra inferior
enum class HomeTab(val label: String) {
    CLIENTES("Clientes"),
    AGENDAMENTOS("Agendamentos")
}

// HomeScreen é a tela principal após o login
// Recebe o token JWT para repassar aos ViewModels
// Adicione onLogout como parâmetro
@Composable
fun HomeScreen(token: String, onLogout: () -> Unit) {

    // Estado que controla qual aba está selecionada
    // Começa na aba de Agendamentos
    var abaSelecionada by remember { mutableStateOf(HomeTab.AGENDAMENTOS) }

    // Cria os ViewModels passando o token
    // viewModel(factory = ...) permite criar ViewModels com parâmetros
    val clienteViewModel: ClienteViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ClienteViewModel(token) as T
            }
        }
    )

    val agendamentoViewModel: AgendamentoViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AgendamentoViewModel(token) as T
            }
        }
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Branco) {
                HomeTab.entries.forEach { aba ->
                    NavigationBarItem(
                        selected = abaSelecionada == aba,
                        onClick = { abaSelecionada = aba },
                        label = {
                            Text(
                                text = aba.label,
                                fontSize = 12.sp,
                                fontWeight = if (abaSelecionada == aba)
                                    FontWeight.Medium else FontWeight.Normal
                            )
                        },
                        icon = {
                            Text(
                                text = if (aba == HomeTab.CLIENTES) "👥" else "📅",
                                fontSize = 20.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VerdeMusgo,
                            selectedTextColor = VerdeMusgo,
                            indicatorColor = VerdeFundo
                        )
                    )
                }
                // Botão de logout na barra inferior
                NavigationBarItem(
                    selected = false,
                    onClick = { onLogout() },
                    label = { Text("Sair", fontSize = 12.sp) },
                    icon = { Text("🚪", fontSize = 20.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VerdeMusgo,
                        selectedTextColor = VerdeMusgo,
                        indicatorColor = VerdeFundo
                    )
                )
            }
        },
        containerColor = VerdeSurface
    ) { paddingValues ->
        when (abaSelecionada) {
            HomeTab.CLIENTES ->
                ClienteScreen(
                    viewModel = clienteViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            HomeTab.AGENDAMENTOS ->
                AgendamentoScreen(
                    viewModel = agendamentoViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
        }
    }
}
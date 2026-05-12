package com.salao.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salao.app.ui.theme.*
import com.salao.app.viewmodel.AgendamentoViewModel
import com.salao.app.viewmodel.ClienteViewModel
import com.salao.app.viewmodel.FiltroStatus
import com.salao.app.viewmodel.RelatorioViewModel
import com.salao.app.viewmodel.ServicoViewModel
import java.time.LocalDate

enum class HomeTab(val label: String) {
    AGENDAMENTOS("Agendamentos"),
    CLIENTES("Clientes"),
    SERVICOS("Servicos"),
    RELATORIOS("Relatorios")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    token: String,
    onLogout: () -> Unit
) {

    var abaSelecionada by remember { mutableStateOf(HomeTab.AGENDAMENTOS) }

    val agendamentoViewModel: AgendamentoViewModel = viewModel(
        key = "agendamento_$token",
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AgendamentoViewModel(token) as T
            }
        }
    )

    val clienteViewModel: ClienteViewModel = viewModel(
        key = "cliente_$token",
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ClienteViewModel(token) as T
            }
        }
    )

    val servicoViewModel: ServicoViewModel = viewModel(
        key = "servico_$token",
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ServicoViewModel(token) as T
            }
        }
    )

    val relatorioViewModel: RelatorioViewModel = viewModel(
        key = "relatorio_$token",
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RelatorioViewModel(token) as T
            }
        }
    )

    LaunchedEffect(abaSelecionada) {
        if (abaSelecionada == HomeTab.AGENDAMENTOS) {
            agendamentoViewModel.selecionarData(LocalDate.now())
            agendamentoViewModel.selecionarFiltro(FiltroStatus.AGENDADO)
        }
    }

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
                                fontWeight = if (abaSelecionada == aba) FontWeight.Medium else FontWeight.Normal
                            )
                        },
                        icon = {
                            Text(
                                text = when (aba) {
                                    HomeTab.AGENDAMENTOS -> "📅"
                                    HomeTab.CLIENTES -> "👥"
                                    HomeTab.SERVICOS -> "✂"
                                    HomeTab.RELATORIOS -> "💰"
                                },
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
            }
        },
        containerColor = VerdeSurface
    ) { paddingValues ->
        when (abaSelecionada) {
            HomeTab.AGENDAMENTOS -> AgendamentoScreen(
                viewModel = agendamentoViewModel,
                modifier = Modifier.padding(paddingValues),
                onLogout = onLogout
            )
            HomeTab.CLIENTES -> ClienteScreen(
                viewModel = clienteViewModel,
                modifier = Modifier.padding(paddingValues),
                onLogout = onLogout
            )
            HomeTab.SERVICOS -> ServicoScreen(
                viewModel = servicoViewModel,
                modifier = Modifier.padding(paddingValues),
                onLogout = onLogout
            )
            HomeTab.RELATORIOS -> RelatorioScreen(
                viewModel = relatorioViewModel,
                modifier = Modifier.padding(paddingValues),
                onLogout = onLogout
            )
        }
    }
}
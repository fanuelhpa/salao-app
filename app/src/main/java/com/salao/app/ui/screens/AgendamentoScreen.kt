package com.salao.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salao.app.data.model.Agendamento
import com.salao.app.ui.theme.*
import com.salao.app.viewmodel.AgendamentoUiState
import com.salao.app.viewmodel.AgendamentoViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AgendamentoScreen(viewModel: AgendamentoViewModel, modifier: Modifier = Modifier) {

    val uiState by viewModel.uiState.collectAsState()

    val isRefreshing = uiState is AgendamentoUiState.Loading

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.carregarAgendamentos() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Agendamentos", color = Branco, fontWeight = FontWeight.Medium)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdeMusgo)
            )
        },
        containerColor = VerdeSurface
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            when (uiState) {
                is AgendamentoUiState.Loading -> {
                    if (!isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = VerdeMusgo
                        )
                    }
                }
                is AgendamentoUiState.Error -> {
                    Text(
                        text = (uiState as AgendamentoUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is AgendamentoUiState.Success -> {
                    val agendamentos = (uiState as AgendamentoUiState.Success).agendamentos
                    if (agendamentos.isEmpty()) {
                        Text(
                            text = "Nenhum agendamento encontrado.",
                            modifier = Modifier.align(Alignment.Center),
                            color = TextoSecundario
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(agendamentos) { agendamento ->
                                AgendamentoCard(agendamento = agendamento)
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = VerdeMusgo
            )
        }
    }
}

@Composable
fun AgendamentoCard(agendamento: Agendamento) {
    val (tagFundo, tagTexto) = when (agendamento.status) {
        "AGENDADO"  -> TagAgendadoFundo  to TagAgendadoTexto
        "CONCLUIDO" -> TagConcluidoFundo to TagConcluidoTexto
        "CANCELADO" -> TagCanceladoFundo to TagCanceladoTexto
        else        -> VerdeFundo        to VerdeMusgo
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Branco),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Divisor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = agendamento.clienteNome,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = TextoPrimario
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = agendamento.servicoNome,
                    fontSize = 13.sp,
                    color = TextoSecundario
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = agendamento.dataHora,
                    fontSize = 13.sp,
                    color = TextoSecundario
                )
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = tagFundo
            ) {
                Text(
                    text = agendamento.status,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = tagTexto,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
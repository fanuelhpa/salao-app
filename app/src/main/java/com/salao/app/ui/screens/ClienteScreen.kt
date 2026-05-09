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
import com.salao.app.data.model.Cliente
import com.salao.app.ui.theme.*
import com.salao.app.viewmodel.ClienteUiState
import com.salao.app.viewmodel.ClienteViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ClienteScreen(viewModel: ClienteViewModel, modifier: Modifier = Modifier) {

    val uiState by viewModel.uiState.collectAsState()

    // isRefreshing controla se o indicador de refresh está visível
    val isRefreshing = uiState is ClienteUiState.Loading

    // rememberPullRefreshState configura o gesto de pull to refresh
    // onRefresh = função chamada quando o usuário puxa a lista para baixo
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.carregarClientes() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Clientes", color = Branco, fontWeight = FontWeight.Medium)
                        if (uiState is ClienteUiState.Success) {
                            val total = (uiState as ClienteUiState.Success).clientes.size
                            Text(
                                "$total cadastrados",
                                color = Branco.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdeMusgo)
            )
        },
        containerColor = VerdeSurface
    ) { paddingValues ->

        // Box com pullRefresh modifier habilita o gesto na tela inteira
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState) // habilita o gesto de pull
        ) {
            when (uiState) {
                is ClienteUiState.Loading -> {
                    // Não mostra o spinner central durante o pull refresh
                    // O indicador do pull refresh já mostra o carregamento
                    if (!isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = VerdeMusgo
                        )
                    }
                }
                is ClienteUiState.Error -> {
                    Text(
                        text = (uiState as ClienteUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ClienteUiState.Success -> {
                    val clientes = (uiState as ClienteUiState.Success).clientes
                    if (clientes.isEmpty()) {
                        Text(
                            text = "Nenhum cliente cadastrado.",
                            modifier = Modifier.align(Alignment.Center),
                            color = TextoSecundario
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(clientes) { cliente ->
                                ClienteCard(cliente = cliente)
                            }
                        }
                    }
                }
            }

            // PullRefreshIndicator deve ser o último elemento do Box
            // para ficar sempre por cima dos outros elementos
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
fun ClienteCard(cliente: Cliente) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Branco),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Divisor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(22.dp),
                color = VerdeFundo
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = cliente.nome.first().uppercase(),
                        color = VerdeMusgo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = cliente.nome,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = TextoPrimario
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = cliente.email,
                    fontSize = 13.sp,
                    color = TextoSecundario
                )
                if (!cliente.telefone.isNullOrBlank()) {
                    Text(
                        text = cliente.telefone,
                        fontSize = 13.sp,
                        color = TextoSecundario
                    )
                }
            }
        }
    }
}
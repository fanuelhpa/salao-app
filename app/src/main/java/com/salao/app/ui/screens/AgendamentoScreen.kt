package com.salao.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendamentoScreen(viewModel: AgendamentoViewModel) {

    val uiState by viewModel.uiState.collectAsState()

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
        ) {
            when (uiState) {
                is AgendamentoUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = VerdeMusgo
                    )
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
        }
    }
}

@Composable
fun AgendamentoCard(agendamento: Agendamento) {

    // Pair é uma classe Kotlin que agrupa dois valores relacionados
    // Aqui usamos para retornar a cor de fundo E a cor do texto da tag juntos
    // "to" é uma função infix do Kotlin que cria um Pair — mais legível que Pair(a, b)
    val (tagFundo, tagTexto) = when (agendamento.status) {
        "AGENDADO"  -> TagAgendadoFundo  to TagAgendadoTexto   // verde musgo claro
        "CONCLUIDO" -> TagConcluidoFundo to TagConcluidoTexto  // âmbar
        "CANCELADO" -> TagCanceladoFundo to TagCanceladoTexto  // vermelho claro
        else        -> VerdeFundo        to VerdeMusgo
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Branco),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Divisor)
    ) {
        // Row com SpaceBetween distribui os filhos nas extremidades
        // (nome/serviço à esquerda, tag de status à direita)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // weight(1f) faz a Column ocupar todo o espaço disponível
            // deixando a tag de status só com o espaço que ela precisa
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

            // Surface com shape arredondado cria a tag pill de status
            // A cor muda dinamicamente conforme o status do agendamento
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
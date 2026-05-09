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
import com.salao.app.data.model.Cliente
import com.salao.app.ui.theme.*
import com.salao.app.viewmodel.ClienteUiState
import com.salao.app.viewmodel.ClienteViewModel

// ClienteScreen recebe o ViewModel que gerencia a lista de clientes
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteScreen(viewModel: ClienteViewModel, modifier: Modifier = Modifier) {

    // Observa o estado do ViewModel — redesenha a tela quando mudar
    val uiState by viewModel.uiState.collectAsState()

    // Scaffold é a estrutura base de uma tela Material3
    // Fornece slots para TopAppBar, BottomBar, FloatingActionButton, etc.
    Scaffold(
        topBar = {
            // TopAppBar é a barra superior da tela
            TopAppBar(
                title = {
                    // Column dentro do title permite colocar título e subtítulo
                    Column {
                        Text("Clientes", color = Branco, fontWeight = FontWeight.Medium)
                        // Exibe a contagem de clientes somente quando o estado for Success
                        if (uiState is ClienteUiState.Success) {
                            val total = (uiState as ClienteUiState.Success).clientes.size
                            Text(
                                "$total cadastrados",
                                color = Branco.copy(alpha = 0.7f), // 70% de opacidade
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdeMusgo)
            )
        },
        containerColor = VerdeSurface // cor de fundo da tela inteira
    ) { paddingValues ->
        // paddingValues contém o padding automático do Scaffold
        // (evita que o conteúdo fique atrás da TopAppBar)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // aplica o padding do Scaffold
        ) {
            // "when" no Kotlin é como o "switch" no Java, mas mais poderoso
            // Aqui usamos para renderizar UI diferente para cada estado
            when (uiState) {

                // Estado de carregamento — mostra spinner centralizado
                is ClienteUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = VerdeMusgo
                    )
                }

                // Estado de erro — mostra mensagem centralizada
                is ClienteUiState.Error -> {
                    Text(
                        text = (uiState as ClienteUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Estado de sucesso — mostra a lista de clientes
                is ClienteUiState.Success -> {
                    val clientes = (uiState as ClienteUiState.Success).clientes

                    if (clientes.isEmpty()) {
                        Text(
                            text = "Nenhum cliente cadastrado.",
                            modifier = Modifier.align(Alignment.Center),
                            color = TextoSecundario
                        )
                    } else {
                        // LazyColumn é o equivalente ao RecyclerView no Compose
                        // Renderiza apenas os itens visíveis na tela (eficiente para listas grandes)
                        // contentPadding = espaço nas bordas da lista
                        // verticalArrangement = espaço entre os itens
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // items() itera sobre a lista e chama ClienteCard para cada item
                            items(clientes) { cliente ->
                                ClienteCard(cliente = cliente)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ClienteCard é um componente reutilizável que representa um cliente na lista
// Separar em função própria mantém o código organizado e reutilizável
@Composable
fun ClienteCard(cliente: Cliente) {
    // Card é um container com fundo branco e borda sutil
    // border = adiciona uma borda fina ao redor do card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Branco),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Divisor)
    ) {
        // Row organiza os filhos horizontalmente (um ao lado do outro)
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar com a inicial do nome do cliente
            // Surface com shape circular cria o círculo colorido
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(22.dp), // metade do tamanho = círculo perfeito
                color = VerdeFundo
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        // first() pega o primeiro caractere do nome
                        // uppercase() transforma em maiúscula
                        text = cliente.nome.first().uppercase(),
                        color = VerdeMusgo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Column com os dados do cliente
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
                // isNullOrBlank() verifica se o telefone é nulo ou vazio
                // O "?" após telefone indica que o campo é nullable (pode ser nulo)
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
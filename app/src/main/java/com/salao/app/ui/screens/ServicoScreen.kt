package com.salao.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salao.app.data.model.Servico
import com.salao.app.ui.theme.*
import com.salao.app.viewmodel.FormState
import com.salao.app.viewmodel.ServicoUiState
import com.salao.app.viewmodel.ServicoViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ServicoScreen(
    viewModel: ServicoViewModel,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val isRefreshing = uiState is ServicoUiState.Loading

    var servicoParaEditar by remember { mutableStateOf<Servico?>(null) }
    var mostrarFormularioCadastro by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.carregarServicos() }
    )

    LaunchedEffect(formState) {
        if (formState is FormState.Sucesso) {
            servicoParaEditar = null
            mostrarFormularioCadastro = false
            viewModel.resetFormState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Servicos", color = Branco, fontWeight = FontWeight.Medium)
                        if (uiState is ServicoUiState.Success) {
                            val total = (uiState as ServicoUiState.Success).servicos.size
                            Text("$total servicos", color = Branco.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarFormularioCadastro = true }) {
                        Text("+", fontSize = 24.sp, color = Branco, fontWeight = FontWeight.Light)
                    }
                    MenuLogout(onLogout = onLogout)
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
                is ServicoUiState.Loading -> {
                    if (!isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = VerdeMusgo
                        )
                    }
                }
                is ServicoUiState.Error -> {
                    Text(
                        text = (uiState as ServicoUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ServicoUiState.Success -> {
                    val servicos = (uiState as ServicoUiState.Success).servicos
                    if (servicos.isEmpty()) {
                        Text(
                            text = "Nenhum servico cadastrado.",
                            modifier = Modifier.align(Alignment.Center),
                            color = TextoSecundario
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 80.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(servicos) { servico ->
                                ServicoCard(
                                    servico = servico,
                                    onClick = { servicoParaEditar = servico }
                                )
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

    // Formulário de cadastro
    if (mostrarFormularioCadastro) {
        ModalBottomSheet(
            onDismissRequest = {
                mostrarFormularioCadastro = false
                viewModel.resetFormState()
            },
            containerColor = Branco
        ) {
            ServicoForm(
                titulo = "Novo Servico",
                nomeInicial = "",
                descricaoInicial = "",
                duracaoInicial = "",
                precoInicial = "",
                formState = formState,
                onSalvar = { nome, descricao, duracao, preco ->
                    viewModel.criarServico(nome, descricao, duracao, preco)
                }
            )
        }
    }

    // Formulário de edição
    servicoParaEditar?.let { servico ->
        ModalBottomSheet(
            onDismissRequest = {
                servicoParaEditar = null
                viewModel.resetFormState()
            },
            containerColor = Branco
        ) {
            ServicoForm(
                titulo = "Editar Servico",
                nomeInicial = servico.nome,
                descricaoInicial = servico.descricao ?: "",
                duracaoInicial = servico.duracaoMinutos.toString(),
                precoInicial = servico.preco.toString(),
                formState = formState,
                onSalvar = { nome, descricao, duracao, preco ->
                    viewModel.atualizarServico(servico.id, nome, descricao, duracao, preco)
                }
            )
        }
    }
}

// Formulário reutilizável para cadastro e edição de serviço
@Composable
fun ServicoForm(
    titulo: String,
    nomeInicial: String,
    descricaoInicial: String,
    duracaoInicial: String,
    precoInicial: String,
    formState: FormState,
    onSalvar: (String, String, Int, Double) -> Unit
) {
    var nome by remember { mutableStateOf(nomeInicial) }
    var descricao by remember { mutableStateOf(descricaoInicial) }
    var duracaoMinutos by remember { mutableStateOf(duracaoInicial) }
    var preco by remember { mutableStateOf(precoInicial) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = titulo,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = TextoPrimario
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerdeMusgo,
                focusedLabelColor = VerdeMusgo,
                cursorColor = VerdeMusgo
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text("Descricao (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerdeMusgo,
                focusedLabelColor = VerdeMusgo,
                cursorColor = VerdeMusgo
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = duracaoMinutos,
                onValueChange = { duracaoMinutos = it },
                label = { Text("Duracao (min)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeMusgo,
                    focusedLabelColor = VerdeMusgo,
                    cursorColor = VerdeMusgo
                )
            )
            OutlinedTextField(
                value = preco,
                onValueChange = { preco = it },
                label = { Text("Preco (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeMusgo,
                    focusedLabelColor = VerdeMusgo,
                    cursorColor = VerdeMusgo
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (formState is FormState.Erro) {
            Text(
                text = (formState as FormState.Erro).message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onSalvar(
                    nome,
                    descricao,
                    duracaoMinutos.toIntOrNull() ?: 0,
                    preco.toDoubleOrNull() ?: 0.0
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeMusgo),
            enabled = nome.isNotBlank() &&
                    duracaoMinutos.isNotBlank() &&
                    preco.isNotBlank() &&
                    formState !is FormState.Loading
        ) {
            if (formState is FormState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Branco,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Salvar", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun ServicoCard(servico: Servico, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = servico.nome,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = TextoPrimario
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${servico.duracaoMinutos} min • R$ ${"%.2f".format(servico.preco)}",
                    fontSize = 13.sp,
                    color = TextoSecundario
                )
                if (!servico.descricao.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = servico.descricao,
                        fontSize = 12.sp,
                        color = TextoSecundario
                    )
                }
            }
            Text("›", fontSize = 20.sp, color = TextoSecundario)
        }
    }
}
package com.salao.app.ui.screens

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
import com.salao.app.data.model.Cliente
import com.salao.app.ui.theme.*
import com.salao.app.viewmodel.ClienteUiState
import com.salao.app.viewmodel.ClienteViewModel
import com.salao.app.viewmodel.FormState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ClienteScreen(viewModel: ClienteViewModel, modifier: Modifier = Modifier) {

    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val isRefreshing = uiState is ClienteUiState.Loading

    // Controla se o bottomsheet de cadastro está aberto
    var mostrarFormulario by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.carregarClientes() }
    )

    // Fecha o formulário e reseta o estado quando o cadastro for bem sucedido
    LaunchedEffect(formState) {
        if (formState is FormState.Sucesso) {
            mostrarFormulario = false
            viewModel.resetFormState()
        }
    }

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
                // Botão + no canto superior direito
                actions = {
                    IconButton(onClick = { mostrarFormulario = true }) {
                        Text("+", fontSize = 24.sp, color = Branco, fontWeight = FontWeight.Light)
                    }
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
                is ClienteUiState.Loading -> {
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

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = VerdeMusgo
            )
        }
    }

    // Formulário de cadastro como BottomSheet
    // Aparece deslizando de baixo para cima quando mostrarFormulario = true
    if (mostrarFormulario) {
        ModalBottomSheet(
            onDismissRequest = {
                mostrarFormulario = false
                viewModel.resetFormState()
            },
            containerColor = Branco
        ) {
            CadastroClienteForm(
                formState = formState,
                onSalvar = { nome, email, telefone ->
                    viewModel.criarCliente(nome, email, telefone)
                }
            )
        }
    }
}

// Formulário de cadastro de cliente
@Composable
fun CadastroClienteForm(
    formState: FormState,
    onSalvar: (String, String, String) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Novo Cliente",
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
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
            value = telefone,
            onValueChange = { telefone = it },
            label = { Text("Telefone (opcional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerdeMusgo,
                focusedLabelColor = VerdeMusgo,
                cursorColor = VerdeMusgo
            )
        )

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
            onClick = { onSalvar(nome, email, telefone) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeMusgo),
            // Desativa o botão se os campos obrigatórios estiverem vazios
            enabled = nome.isNotBlank() && email.isNotBlank() && formState !is FormState.Loading
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
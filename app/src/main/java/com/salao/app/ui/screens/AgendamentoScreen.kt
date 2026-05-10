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
import com.salao.app.data.model.Cliente
import com.salao.app.data.model.Servico
import com.salao.app.ui.theme.*
import com.salao.app.viewmodel.AgendamentoUiState
import com.salao.app.viewmodel.AgendamentoViewModel
import com.salao.app.viewmodel.FormState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AgendamentoScreen(viewModel: AgendamentoViewModel, modifier: Modifier = Modifier) {

    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val clientes by viewModel.clientes.collectAsState()
    val servicos by viewModel.servicos.collectAsState()
    val isRefreshing = uiState is AgendamentoUiState.Loading

    var mostrarFormulario by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.carregarAgendamentos() }
    )

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
                    Text("Agendamentos", color = Branco, fontWeight = FontWeight.Medium)
                },
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

    if (mostrarFormulario) {
        ModalBottomSheet(
            onDismissRequest = {
                mostrarFormulario = false
                viewModel.resetFormState()
            },
            containerColor = Branco
        ) {
            CadastroAgendamentoForm(
                clientes = clientes,
                servicos = servicos,
                formState = formState,
                onSalvar = { clienteId, servicoId, dataHora, observacoes ->
                    viewModel.criarAgendamento(clienteId, servicoId, dataHora, observacoes)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroAgendamentoForm(
    clientes: List<Cliente>,
    servicos: List<Servico>,
    formState: FormState,
    onSalvar: (Long, Long, String, String) -> Unit
) {
    var clienteSelecionado by remember { mutableStateOf<Cliente?>(null) }
    var servicoSelecionado by remember { mutableStateOf<Servico?>(null) }
    var observacoes by remember { mutableStateOf("") }
    var dataSelecionada by remember { mutableStateOf("") }
    var horaSelecionada by remember { mutableStateOf("") }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var clienteDropdownAberto by remember { mutableStateOf(false) }
    var servicoDropdownAberto by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = true
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Novo Agendamento",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = TextoPrimario
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Dropdown de Cliente
        ExposedDropdownMenuBox(
            expanded = clienteDropdownAberto,
            onExpandedChange = { clienteDropdownAberto = it }
        ) {
            OutlinedTextField(
                value = clienteSelecionado?.nome ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Cliente") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = clienteDropdownAberto)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeMusgo,
                    focusedLabelColor = VerdeMusgo
                )
            )
            ExposedDropdownMenu(
                expanded = clienteDropdownAberto,
                onDismissRequest = { clienteDropdownAberto = false }
            ) {
                clientes.forEach { cliente ->
                    DropdownMenuItem(
                        text = { Text(cliente.nome) },
                        onClick = {
                            clienteSelecionado = cliente
                            clienteDropdownAberto = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dropdown de Serviço
        ExposedDropdownMenuBox(
            expanded = servicoDropdownAberto,
            onExpandedChange = { servicoDropdownAberto = it }
        ) {
            OutlinedTextField(
                value = servicoSelecionado?.nome ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Servico") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = servicoDropdownAberto)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeMusgo,
                    focusedLabelColor = VerdeMusgo
                )
            )
            ExposedDropdownMenu(
                expanded = servicoDropdownAberto,
                onDismissRequest = { servicoDropdownAberto = false }
            ) {
                servicos.forEach { servico ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(servico.nome, fontSize = 14.sp)
                                Text(
                                    "${servico.duracaoMinutos} min • R$ ${"%.2f".format(servico.preco)}",
                                    fontSize = 12.sp,
                                    color = TextoSecundario
                                )
                            }
                        },
                        onClick = {
                            servicoSelecionado = servico
                            servicoDropdownAberto = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botão que abre o calendário
        OutlinedButton(
            onClick = { mostrarDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeMusgo),
            border = androidx.compose.foundation.BorderStroke(1.dp, VerdeMusgo)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (dataSelecionada.isBlank()) "Selecionar data" else dataSelecionada,
                    color = if (dataSelecionada.isBlank()) TextoSecundario else TextoPrimario
                )
                Text("📅", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botão que abre o seletor de hora
        OutlinedButton(
            onClick = { mostrarTimePicker = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeMusgo),
            border = androidx.compose.foundation.BorderStroke(1.dp, VerdeMusgo)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (horaSelecionada.isBlank()) "Selecionar hora" else horaSelecionada,
                    color = if (horaSelecionada.isBlank()) TextoSecundario else TextoPrimario
                )
                Text("🕐", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = observacoes,
            onValueChange = { observacoes = it },
            label = { Text("Observacoes (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
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
            onClick = {
                val partesData = dataSelecionada.split("/")
                val dataFormatada = "${partesData[2]}-${partesData[1]}-${partesData[0]}"
                val dataHora = "${dataFormatada}T${horaSelecionada}:00"
                onSalvar(
                    clienteSelecionado!!.id,
                    servicoSelecionado!!.id,
                    dataHora,
                    observacoes
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeMusgo),
            enabled = clienteSelecionado != null &&
                    servicoSelecionado != null &&
                    dataSelecionada.isNotBlank() &&
                    horaSelecionada.isNotBlank() &&
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

        // Dialog do calendário
        if (mostrarDatePicker) {
            DatePickerDialog(
                onDismissRequest = { mostrarDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val calendar = java.util.Calendar.getInstance(
                                    java.util.TimeZone.getTimeZone("UTC")
                                )
                                calendar.timeInMillis = millis
                                val dia = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                    .toString().padStart(2, '0')
                                val mes = (calendar.get(java.util.Calendar.MONTH) + 1)
                                    .toString().padStart(2, '0')
                                val ano = calendar.get(java.util.Calendar.YEAR)
                                dataSelecionada = "$dia/$mes/$ano"
                            }
                            mostrarDatePicker = false
                        }
                    ) {
                        Text("Confirmar", color = VerdeMusgo)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDatePicker = false }) {
                        Text("Cancelar", color = TextoSecundario)
                    }
                },
                colors = DatePickerDefaults.colors(containerColor = Branco)
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = VerdeMusgo,
                        todayDateBorderColor = VerdeMusgo,
                        currentYearContentColor = VerdeMusgo,
                        selectedYearContainerColor = VerdeMusgo
                    )
                )
            }
        }

        // Dialog do seletor de hora
        if (mostrarTimePicker) {
            AlertDialog(
                onDismissRequest = { mostrarTimePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val hora = timePickerState.hour.toString().padStart(2, '0')
                            val minuto = timePickerState.minute.toString().padStart(2, '0')
                            horaSelecionada = "$hora:$minuto"
                            mostrarTimePicker = false
                        }
                    ) {
                        Text("Confirmar", color = VerdeMusgo)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarTimePicker = false }) {
                        Text("Cancelar", color = TextoSecundario)
                    }
                },
                containerColor = Branco,
                text = {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = VerdeFundo,
                            selectorColor = VerdeMusgo,
                            containerColor = Branco,
                            timeSelectorSelectedContainerColor = VerdeMusgo,
                            timeSelectorSelectedContentColor = Branco
                        )
                    )
                }
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
                    text = formatarDataHora(agendamento.dataHora),
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
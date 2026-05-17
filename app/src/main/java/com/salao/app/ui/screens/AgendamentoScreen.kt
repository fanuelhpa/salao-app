package com.salao.app.ui.screens

import androidx.compose.foundation.clickable
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
import com.salao.app.viewmodel.AgendamentoViewModel
import com.salao.app.viewmodel.FiltroStatus
import com.salao.app.viewmodel.FormState
import com.salao.app.viewmodel.PagamentoState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.zIndex

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AgendamentoScreen(
    viewModel: AgendamentoViewModel,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    mostrarFormularioCadastro: Boolean = false,
    onFecharFormulario: () -> Unit = {}
) {

    val agendamentos by viewModel.agendamentosFiltrados.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val clientes by viewModel.clientes.collectAsState()
    val servicos by viewModel.servicos.collectAsState()
    val carregando by viewModel.carregando.collectAsState()
    val dataSelecionada by viewModel.dataSelecionada.collectAsState()
    val filtroStatus by viewModel.filtroStatus.collectAsState()
    val agendamentosPagos by viewModel.agendamentosPagos.collectAsState()

    var mostrarFormularioCadastro by remember { mutableStateOf(false) }
    var agendamentoParaEditar by remember { mutableStateOf<Agendamento?>(null) }
    var mostrarDatePickerFiltro by remember { mutableStateOf(false) }
    val datePickerFiltroState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val pullRefreshState = rememberPullRefreshState(
        refreshing = carregando,
        onRefresh = { viewModel.carregarAgendamentos() }
    )

    LaunchedEffect(formState) {
        if (formState is FormState.Sucesso) {
            mostrarFormularioCadastro = false
            agendamentoParaEditar = null
            viewModel.resetFormState()
        }
    }

    val context = LocalContext.current

    val dataFormatada = remember(dataSelecionada) {
        val hoje = LocalDate.now()
        when (dataSelecionada) {
            hoje -> "Hoje"
            hoje.plusDays(1) -> "Amanha"
            hoje.minusDays(1) -> "Ontem"
            else -> dataSelecionada.format(
                DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Agendamentos", color = Branco, fontWeight = FontWeight.Medium)
                        Text(
                            text = if (filtroStatus == FiltroStatus.PENDENTES) "Dias anteriores"
                            else dataFormatada,
                            color = Branco.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                },
                actions = {
                    if (filtroStatus != FiltroStatus.PENDENTES) {
                        IconButton(onClick = { mostrarDatePickerFiltro = true }) {
                            Text("📅", fontSize = 18.sp)
                        }
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
                .padding(paddingValues),
            contentAlignment = Alignment.TopStart
        ) {
        // Conteúdo principal com pull refresh
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FiltroStatus.entries.forEach { filtro ->
                        val selecionado = filtroStatus == filtro
                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = selecionado,
                            onClick = { viewModel.selecionarFiltro(filtro) },
                            label = {
                                Text(
                                    text = when (filtro) {
                                        FiltroStatus.AGENDADO -> "Agendados"
                                        FiltroStatus.CANCELADO -> "Cancelados"
                                        FiltroStatus.PENDENTES -> "Pendentes"
                                    },
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VerdeMusgo,
                                selectedLabelColor = Branco,
                                containerColor = Branco,
                                labelColor = VerdeMusgo
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selecionado,
                                borderColor = VerdeMusgo,
                                selectedBorderColor = VerdeMusgo
                            )
                        )
                    }
                }

                if (carregando) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VerdeMusgo)
                    }
                } else if (agendamentos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Nenhum agendamento encontrado.", color = TextoSecundario)
                    }
                } else {
                    if (filtroStatus == FiltroStatus.AGENDADO) {
                        val scrollState = rememberScrollState()
                        val density = androidx.compose.ui.platform.LocalDensity.current
                        LaunchedEffect(agendamentos) {
                            if (agendamentos.isNotEmpty()) {
                                val primeiroAgendamento = agendamentos.minByOrNull { it.dataHora }
                                primeiroAgendamento?.let { ag ->
                                    val partes = ag.dataHora.substring(11, 16).split(":")
                                    val minutosTotal = partes[0].toInt() * 60 + partes[1].toInt()
                                    val pixelsPorMinuto = 4f
                                    val targetPx = with(density) {
                                        (minutosTotal * pixelsPorMinuto).dp.toPx().toInt()
                                    }
                                    scrollState.animateScrollTo(targetPx)
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(bottom = 80.dp)
                        ) {
                            AgendaTimeline(
                                agendamentos = agendamentos,
                                pagos = agendamentosPagos,
                                onAgendamentoClick = { agendamentoParaEditar = it }
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 0.dp,
                                bottom = 80.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(agendamentos) { agendamento ->
                                AgendamentoCard(
                                    agendamento = agendamento,
                                    pago = agendamento.id in agendamentosPagos.keys,
                                    onClick = { agendamentoParaEditar = agendamento }
                                )
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = carregando,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = VerdeMusgo
            )
        }

            FloatingActionButton(
                onClick = { mostrarFormularioCadastro = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 100.dp), // aumenta o padding bottom
                containerColor = VerdeMusgo,
                contentColor = Branco,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("+", fontSize = 28.sp, fontWeight = FontWeight.Light)
            }
    }
}

    // Dialog do calendário para filtrar por dia
    if (mostrarDatePickerFiltro) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerFiltro = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerFiltroState.selectedDateMillis?.let { millis ->
                            val calendar = java.util.Calendar.getInstance(
                                java.util.TimeZone.getTimeZone("UTC")
                            )
                            calendar.timeInMillis = millis
                            val data = LocalDate.of(
                                calendar.get(java.util.Calendar.YEAR),
                                calendar.get(java.util.Calendar.MONTH) + 1,
                                calendar.get(java.util.Calendar.DAY_OF_MONTH)
                            )
                            viewModel.selecionarData(data)
                        }
                        mostrarDatePickerFiltro = false
                    }
                ) { Text("Confirmar", color = VerdeMusgo) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePickerFiltro = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = Branco)
        ) {
            DatePicker(
                state = datePickerFiltroState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = VerdeMusgo,
                    todayDateBorderColor = VerdeMusgo,
                    currentYearContentColor = VerdeMusgo,
                    selectedYearContainerColor = VerdeMusgo
                )
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

    // Formulário de edição
    agendamentoParaEditar?.let { agendamento ->

        val pagamentoState by viewModel.pagamentoState.collectAsState()
        var mostrarFormPagamento by remember { mutableStateOf(false) }

        // Busca o preço do serviço para sugerir no pagamento
        val precoSugerido = viewModel.servicos.value
            .find { it.id == agendamento.servicoId }?.preco ?: 0.0

        LaunchedEffect(pagamentoState) {
            if (pagamentoState is PagamentoState.Sucesso) {
                mostrarFormPagamento = false
                agendamentoParaEditar = null
                viewModel.resetPagamentoState()
                Toast.makeText(
                    context,
                    "Pagamento registrado com sucesso!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        if (mostrarFormPagamento) {
            ModalBottomSheet(
                onDismissRequest = {
                    mostrarFormPagamento = false
                    viewModel.resetPagamentoState()
                },
                containerColor = Branco
            ) {
                PagamentoForm(
                    agendamento = agendamento,
                    precoSugerido = precoSugerido,
                    pagamentoState = pagamentoState,
                    onRegistrar = { valor, metodo, data ->
                        viewModel.registrarPagamento(agendamento.id, valor, metodo, data)
                    },
                    onDismiss = {
                        mostrarFormPagamento = false
                        agendamentoParaEditar = null
                        viewModel.resetPagamentoState()
                    }
                )
            }
        } else {
            ModalBottomSheet(
                onDismissRequest = {
                    agendamentoParaEditar = null
                    viewModel.resetFormState()
                },
                containerColor = Branco
            ) {
                EdicaoAgendamentoForm(
                    agendamento = agendamento,
                    servicos = servicos,
                    formState = formState,
                    pagamentoState = pagamentoState,
                    precoSugerido = precoSugerido,
                    jaPago = agendamento.id in agendamentosPagos.keys,
                    onCancelar = { viewModel.cancelarAgendamento(agendamento.id) },
                    onConcluir = { viewModel.concluirAgendamento(agendamento.id) },
                    onAlterarServico = { servicoId, dataHora ->
                        viewModel.atualizarAgendamento(agendamento.id, servicoId, dataHora)
                    },
                    onRegistrarPagamento = { valor, metodo, data ->
                        viewModel.registrarPagamento(agendamento.id, valor, metodo, data)
                    },
                    onAbrirPagamento = { mostrarFormPagamento = true } // abre o form
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdicaoAgendamentoForm(
    agendamento: Agendamento,
    servicos: List<Servico>,
    formState: FormState,
    pagamentoState: PagamentoState,
    precoSugerido: Double,
    jaPago: Boolean,
    onAbrirPagamento: () -> Unit,
    onCancelar: () -> Unit,
    onConcluir: () -> Unit,
    onAlterarServico: (Long, String) -> Unit,
    onRegistrarPagamento: (Double, String, String) -> Unit,
){
    var servicoSelecionado by remember {
        mutableStateOf(servicos.find { it.id == agendamento.servicoId })
    }
    var dataSelecionada by remember { mutableStateOf("") }
    var horaSelecionada by remember { mutableStateOf("") }
    var servicoDropdownAberto by remember { mutableStateOf(false) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarTimePicker by remember { mutableStateOf(false) }
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
        Text(text = "Editar Agendamento", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextoPrimario)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = agendamento.clienteNome, fontSize = 14.sp, color = TextoSecundario)
        Spacer(modifier = Modifier.height(24.dp))

        ExposedDropdownMenuBox(expanded = servicoDropdownAberto, onExpandedChange = { servicoDropdownAberto = it }) {
            OutlinedTextField(
                value = servicoSelecionado?.nome ?: agendamento.servicoNome,
                onValueChange = {},
                readOnly = true,
                label = { Text("Servico") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = servicoDropdownAberto) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdeMusgo, focusedLabelColor = VerdeMusgo)
            )
            ExposedDropdownMenu(expanded = servicoDropdownAberto, onDismissRequest = { servicoDropdownAberto = false }) {
                servicos.forEach { servico ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(servico.nome, fontSize = 14.sp)
                                Text("${servico.duracaoMinutos} min • R$ ${"%.2f".format(servico.preco)}", fontSize = 12.sp, color = TextoSecundario)
                            }
                        },
                        onClick = { servicoSelecionado = servico; servicoDropdownAberto = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { mostrarDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeMusgo),
            border = androidx.compose.foundation.BorderStroke(1.dp, VerdeMusgo)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (dataSelecionada.isBlank()) formatarDataHora(agendamento.dataHora).split(" ")[0] else dataSelecionada, color = TextoPrimario)
                Text("📅", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { mostrarTimePicker = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeMusgo),
            border = androidx.compose.foundation.BorderStroke(1.dp, VerdeMusgo)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (horaSelecionada.isBlank()) formatarDataHora(agendamento.dataHora).split(" ")[1] else horaSelecionada, color = TextoPrimario)
                Text("🕐", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (formState is FormState.Erro) {
            Text(text = (formState as FormState.Erro).message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (agendamento.status == "AGENDADO") {
            Button(
                onClick = {
                    val dataFinal = if (dataSelecionada.isBlank()) agendamento.dataHora.split("T")[0]
                    else { val p = dataSelecionada.split("/"); "${p[2]}-${p[1]}-${p[0]}" }
                    val horaFinal = if (horaSelecionada.isBlank()) agendamento.dataHora.split("T")[1].substring(0, 5) else horaSelecionada
                    onAlterarServico(servicoSelecionado?.id ?: agendamento.servicoId, "${dataFinal}T${horaFinal}:00")
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeMusgo),
                enabled = formState !is FormState.Loading
            ) {
                if (formState is FormState.Loading) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Branco, strokeWidth = 2.dp)
                else Text("Salvar alteracoes", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onConcluir,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TagConcluidoFundo, contentColor = TagConcluidoTexto),
                enabled = formState !is FormState.Loading
            ) { Text("Concluir agendamento", fontSize = 16.sp, fontWeight = FontWeight.Medium) }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onCancelar,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TagCanceladoTexto),
                border = androidx.compose.foundation.BorderStroke(1.dp, TagCanceladoTexto),
                enabled = formState !is FormState.Loading
            ) { Text("Cancelar agendamento", fontSize = 16.sp, fontWeight = FontWeight.Medium) }

        } else {
            if (agendamento.status == "CONCLUIDO") {
                if (!jaPago) {
                    // Mostra o botão só se ainda não foi pago
                    Button(
                        onClick = { onAbrirPagamento() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TagConcluidoFundo,
                            contentColor = TagConcluidoTexto
                        )
                    ) {
                        Text("Registrar Pagamento", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                } else {
                    // Agendamento já pago — mostra mensagem informativa
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = EventoVerdeFundo
                    ) {
                        Text(
                            text = "Pagamento já registrado.",
                            modifier = Modifier.padding(16.dp),
                            color = EventoVerdeTexto,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = TagCanceladoFundo
                ) {
                    Text(
                        text = "Este agendamento foi cancelado.",
                        modifier = Modifier.padding(16.dp),
                        color = TagCanceladoTexto,
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (mostrarDatePicker) {
            DatePickerDialog(
                onDismissRequest = { mostrarDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                            calendar.timeInMillis = millis
                            val dia = calendar.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                            val mes = (calendar.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
                            val ano = calendar.get(java.util.Calendar.YEAR)
                            dataSelecionada = "$dia/$mes/$ano"
                        }
                        mostrarDatePicker = false
                    }) { Text("Confirmar", color = VerdeMusgo) }
                },
                dismissButton = { TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar", color = TextoSecundario) } },
                colors = DatePickerDefaults.colors(containerColor = Branco)
            ) {
                DatePicker(state = datePickerState, colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = VerdeMusgo, todayDateBorderColor = VerdeMusgo,
                    currentYearContentColor = VerdeMusgo, selectedYearContainerColor = VerdeMusgo))
            }
        }

        if (mostrarTimePicker) {
            AlertDialog(
                onDismissRequest = { mostrarTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val hora = timePickerState.hour.toString().padStart(2, '0')
                        val minuto = timePickerState.minute.toString().padStart(2, '0')
                        horaSelecionada = "$hora:$minuto"
                        mostrarTimePicker = false
                    }) { Text("Confirmar", color = VerdeMusgo) }
                },
                dismissButton = { TextButton(onClick = { mostrarTimePicker = false }) { Text("Cancelar", color = TextoSecundario) } },
                containerColor = Branco,
                text = {
                    TimePicker(state = timePickerState, colors = TimePickerDefaults.colors(
                        clockDialColor = VerdeFundo, selectorColor = VerdeMusgo, containerColor = Branco,
                        timeSelectorSelectedContainerColor = VerdeMusgo, timeSelectorSelectedContentColor = Branco))
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
    val timePickerState = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = true)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
        Text(text = "Novo Agendamento", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextoPrimario)
        Spacer(modifier = Modifier.height(24.dp))

        ExposedDropdownMenuBox(expanded = clienteDropdownAberto, onExpandedChange = { clienteDropdownAberto = it }) {
            OutlinedTextField(
                value = clienteSelecionado?.nome ?: "", onValueChange = {}, readOnly = true,
                label = { Text("Cliente") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clienteDropdownAberto) },
                modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdeMusgo, focusedLabelColor = VerdeMusgo)
            )
            ExposedDropdownMenu(expanded = clienteDropdownAberto, onDismissRequest = { clienteDropdownAberto = false }) {
                clientes.forEach { cliente ->
                    DropdownMenuItem(text = { Text(cliente.nome) }, onClick = { clienteSelecionado = cliente; clienteDropdownAberto = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(expanded = servicoDropdownAberto, onExpandedChange = { servicoDropdownAberto = it }) {
            OutlinedTextField(
                value = servicoSelecionado?.nome ?: "", onValueChange = {}, readOnly = true,
                label = { Text("Servico") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = servicoDropdownAberto) },
                modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdeMusgo, focusedLabelColor = VerdeMusgo)
            )
            ExposedDropdownMenu(expanded = servicoDropdownAberto, onDismissRequest = { servicoDropdownAberto = false }) {
                servicos.forEach { servico ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(servico.nome, fontSize = 14.sp)
                                Text("${servico.duracaoMinutos} min • R$ ${"%.2f".format(servico.preco)}", fontSize = 12.sp, color = TextoSecundario)
                            }
                        },
                        onClick = { servicoSelecionado = servico; servicoDropdownAberto = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { mostrarDatePicker = true }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeMusgo),
            border = androidx.compose.foundation.BorderStroke(1.dp, VerdeMusgo)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (dataSelecionada.isBlank()) "Selecionar data" else dataSelecionada, color = if (dataSelecionada.isBlank()) TextoSecundario else TextoPrimario)
                Text("📅", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { mostrarTimePicker = true }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeMusgo),
            border = androidx.compose.foundation.BorderStroke(1.dp, VerdeMusgo)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (horaSelecionada.isBlank()) "Selecionar hora" else horaSelecionada, color = if (horaSelecionada.isBlank()) TextoSecundario else TextoPrimario)
                Text("🕐", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = observacoes, onValueChange = { observacoes = it },
            label = { Text("Observacoes (opcional)") }, modifier = Modifier.fillMaxWidth(), maxLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdeMusgo, focusedLabelColor = VerdeMusgo, cursorColor = VerdeMusgo)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (formState is FormState.Erro) {
            Text(text = (formState as FormState.Erro).message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val partesData = dataSelecionada.split("/")
                val dataFormatada = "${partesData[2]}-${partesData[1]}-${partesData[0]}"
                onSalvar(clienteSelecionado!!.id, servicoSelecionado!!.id, "${dataFormatada}T${horaSelecionada}:00", observacoes)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeMusgo),
            enabled = clienteSelecionado != null && servicoSelecionado != null &&
                    dataSelecionada.isNotBlank() && horaSelecionada.isNotBlank() && formState !is FormState.Loading
        ) {
            if (formState is FormState.Loading) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Branco, strokeWidth = 2.dp)
            else Text("Salvar", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        if (mostrarDatePicker) {
            DatePickerDialog(
                onDismissRequest = { mostrarDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                            calendar.timeInMillis = millis
                            val dia = calendar.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                            val mes = (calendar.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
                            val ano = calendar.get(java.util.Calendar.YEAR)
                            dataSelecionada = "$dia/$mes/$ano"
                        }
                        mostrarDatePicker = false
                    }) { Text("Confirmar", color = VerdeMusgo) }
                },
                dismissButton = { TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar", color = TextoSecundario) } },
                colors = DatePickerDefaults.colors(containerColor = Branco)
            ) {
                DatePicker(state = datePickerState, colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = VerdeMusgo, todayDateBorderColor = VerdeMusgo,
                    currentYearContentColor = VerdeMusgo, selectedYearContainerColor = VerdeMusgo))
            }
        }

        if (mostrarTimePicker) {
            AlertDialog(
                onDismissRequest = { mostrarTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val hora = timePickerState.hour.toString().padStart(2, '0')
                        val minuto = timePickerState.minute.toString().padStart(2, '0')
                        horaSelecionada = "$hora:$minuto"
                        mostrarTimePicker = false
                    }) { Text("Confirmar", color = VerdeMusgo) }
                },
                dismissButton = { TextButton(onClick = { mostrarTimePicker = false }) { Text("Cancelar", color = TextoSecundario) } },
                containerColor = Branco,
                text = {
                    TimePicker(state = timePickerState, colors = TimePickerDefaults.colors(
                        clockDialColor = VerdeFundo, selectorColor = VerdeMusgo, containerColor = Branco,
                        timeSelectorSelectedContainerColor = VerdeMusgo, timeSelectorSelectedContentColor = Branco))
                }
            )
        }
    }
}

@Composable
fun AgendamentoCard(agendamento: Agendamento, pago: Boolean = false, onClick: () -> Unit) {
    val (tagFundo, tagTexto) = when {
        agendamento.status == "AGENDADO"  -> TagAgendadoFundo  to TagAgendadoTexto
        agendamento.status == "CONCLUIDO" && pago -> EventoVerdeBorda to Branco
        agendamento.status == "CONCLUIDO" && !pago -> EventoAmareloBorda to Branco
        agendamento.status == "CANCELADO" -> TagCanceladoFundo to TagCanceladoTexto
        else -> VerdeFundo to VerdeMusgo
    }

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
            Column(horizontalAlignment = Alignment.End) {
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
                // Tag de PAGO ou Nao pago
                if (agendamento.status == "CONCLUIDO") {
                    if (pago) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = EventoVerdeBorda
                        ) {
                            Text(
                                text = "PAGO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Branco,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Não pago",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = EventoAmareloBorda
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("›", fontSize = 20.sp, color = TextoSecundario)
            }
        }
    }
}
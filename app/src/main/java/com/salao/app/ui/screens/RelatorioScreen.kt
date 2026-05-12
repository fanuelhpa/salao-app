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
import com.salao.app.data.model.Pagamento
import com.salao.app.ui.theme.*
import com.salao.app.viewmodel.RelatorioUiState
import com.salao.app.viewmodel.RelatorioViewModel
import com.salao.app.viewmodel.TipoRelatorio
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioScreen(
    viewModel: RelatorioViewModel,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val tipoSelecionado by viewModel.tipoSelecionado.collectAsState()

    // Estados dos filtros
    var dataSelecionada by remember { mutableStateOf(LocalDate.now()) }
    var mesSelecionado by remember { mutableStateOf(LocalDate.now().monthValue) }
    var anoSelecionado by remember { mutableStateOf(LocalDate.now().year) }
    var dataInicioSelecionada by remember { mutableStateOf(LocalDate.now()) }
    var dataFimSelecionada by remember { mutableStateOf(LocalDate.now()) }

    // DatePickers
    var mostrarDatePickerDia by remember { mutableStateOf(false) }
    var mostrarDatePickerInicio by remember { mutableStateOf(false) }
    var mostrarDatePickerFim by remember { mutableStateOf(false) }
    val datePickerDiaState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    val datePickerInicioState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    val datePickerFimState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Relatorio Financeiro", color = Branco, fontWeight = FontWeight.Medium)
                },
                actions = { MenuLogout(onLogout = onLogout) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdeMusgo)
            )
        },
        containerColor = VerdeSurface
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Chips de tipo de relatório
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TipoRelatorio.entries.forEach { tipo ->
                        val selecionado = tipoSelecionado == tipo
                        FilterChip(
                            selected = selecionado,
                            onClick = { viewModel.selecionarTipo(tipo) },
                            label = {
                                Text(
                                    text = when (tipo) {
                                        TipoRelatorio.DIA -> "Dia"
                                        TipoRelatorio.MES -> "Mes"
                                        TipoRelatorio.PERIODO -> "Periodo"
                                        TipoRelatorio.ANO -> "Ano"
                                    },
                                    fontSize = 12.sp
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
            }

            // Filtros por tipo
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Branco),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Divisor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        when (tipoSelecionado) {

                            TipoRelatorio.DIA -> {
                                OutlinedButton(
                                    onClick = { mostrarDatePickerDia = true },
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
                                            text = dataSelecionada.format(
                                                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                            ),
                                            color = TextoPrimario
                                        )
                                        Text("📅", fontSize = 18.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.buscarPorDia(dataSelecionada) },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VerdeMusgo)
                                ) { Text("Gerar Relatorio") }
                            }

                            TipoRelatorio.MES -> {
                                val meses = listOf("Janeiro", "Fevereiro", "Marco", "Abril", "Maio",
                                    "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
                                var mesDropdownAberto by remember { mutableStateOf(false) }

                                ExposedDropdownMenuBox(
                                    expanded = mesDropdownAberto,
                                    onExpandedChange = { mesDropdownAberto = it }
                                ) {
                                    OutlinedTextField(
                                        value = meses[mesSelecionado - 1],
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Mes") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mesDropdownAberto) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = VerdeMusgo,
                                            focusedLabelColor = VerdeMusgo
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded = mesDropdownAberto,
                                        onDismissRequest = { mesDropdownAberto = false }
                                    ) {
                                        meses.forEachIndexed { index, mes ->
                                            DropdownMenuItem(
                                                text = { Text(mes) },
                                                onClick = {
                                                    mesSelecionado = index + 1
                                                    mesDropdownAberto = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Seletor de ano
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { anoSelecionado-- }) {
                                        Text("‹", fontSize = 24.sp, color = VerdeMusgo)
                                    }
                                    Text(
                                        text = anoSelecionado.toString(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextoPrimario,
                                        modifier = Modifier.weight(1f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    IconButton(onClick = { anoSelecionado++ }) {
                                        Text("›", fontSize = 24.sp, color = VerdeMusgo)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.buscarPorMes(anoSelecionado, mesSelecionado) },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VerdeMusgo)
                                ) { Text("Gerar Relatorio") }
                            }

                            TipoRelatorio.PERIODO -> {
                                OutlinedButton(
                                    onClick = { mostrarDatePickerInicio = true },
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
                                            text = "Inicio: ${dataInicioSelecionada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                                            color = TextoPrimario
                                        )
                                        Text("📅", fontSize = 18.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { mostrarDatePickerFim = true },
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
                                            text = "Fim: ${dataFimSelecionada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                                            color = TextoPrimario
                                        )
                                        Text("📅", fontSize = 18.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.buscarPorPeriodo(dataInicioSelecionada, dataFimSelecionada) },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VerdeMusgo)
                                ) { Text("Gerar Relatorio") }
                            }

                            TipoRelatorio.ANO -> {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { anoSelecionado-- }) {
                                        Text("‹", fontSize = 24.sp, color = VerdeMusgo)
                                    }
                                    Text(
                                        text = anoSelecionado.toString(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextoPrimario,
                                        modifier = Modifier.weight(1f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    IconButton(onClick = { anoSelecionado++ }) {
                                        Text("›", fontSize = 24.sp, color = VerdeMusgo)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.buscarPorAno(anoSelecionado) },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VerdeMusgo)
                                ) { Text("Gerar Relatorio") }
                            }
                        }
                    }
                }
            }

            // Resultado do relatório
            when (uiState) {
                is RelatorioUiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = VerdeMusgo)
                        }
                    }
                }
                is RelatorioUiState.Error -> {
                    item {
                        Text(
                            text = (uiState as RelatorioUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is RelatorioUiState.Success -> {
                    val pagamentos = (uiState as RelatorioUiState.Success).pagamentos

                    if (pagamentos.isEmpty()) {
                        item {
                            Text(
                                text = "Nenhum pagamento encontrado no periodo.",
                                color = TextoSecundario,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        items(pagamentos) { pagamento ->
                            PagamentoCard(pagamento = pagamento)
                        }

                        // Card de total
                        item {
                            val total = pagamentos.sumOf { it.valor }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = VerdeMusgo)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Total recebido",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Branco
                                    )
                                    Text(
                                        text = "R$ ${"%.2f".format(total)}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Branco
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    // DatePicker para dia
    if (mostrarDatePickerDia) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerDia = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerDiaState.selectedDateMillis?.let { millis ->
                        val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = millis
                        dataSelecionada = LocalDate.of(
                            calendar.get(java.util.Calendar.YEAR),
                            calendar.get(java.util.Calendar.MONTH) + 1,
                            calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        )
                    }
                    mostrarDatePickerDia = false
                }) { Text("Confirmar", color = VerdeMusgo) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePickerDia = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = Branco)
        ) {
            DatePicker(
                state = datePickerDiaState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = VerdeMusgo,
                    todayDateBorderColor = VerdeMusgo,
                    currentYearContentColor = VerdeMusgo,
                    selectedYearContainerColor = VerdeMusgo
                )
            )
        }
    }

    // DatePicker para início do período
    if (mostrarDatePickerInicio) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerInicio = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerInicioState.selectedDateMillis?.let { millis ->
                        val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = millis
                        dataInicioSelecionada = LocalDate.of(
                            calendar.get(java.util.Calendar.YEAR),
                            calendar.get(java.util.Calendar.MONTH) + 1,
                            calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        )
                    }
                    mostrarDatePickerInicio = false
                }) { Text("Confirmar", color = VerdeMusgo) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePickerInicio = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = Branco)
        ) {
            DatePicker(
                state = datePickerInicioState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = VerdeMusgo,
                    todayDateBorderColor = VerdeMusgo,
                    currentYearContentColor = VerdeMusgo,
                    selectedYearContainerColor = VerdeMusgo
                )
            )
        }
    }

    // DatePicker para fim do período
    if (mostrarDatePickerFim) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerFim = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerFimState.selectedDateMillis?.let { millis ->
                        val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = millis
                        dataFimSelecionada = LocalDate.of(
                            calendar.get(java.util.Calendar.YEAR),
                            calendar.get(java.util.Calendar.MONTH) + 1,
                            calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        )
                    }
                    mostrarDatePickerFim = false
                }) { Text("Confirmar", color = VerdeMusgo) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePickerFim = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = Branco)
        ) {
            DatePicker(
                state = datePickerFimState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = VerdeMusgo,
                    todayDateBorderColor = VerdeMusgo,
                    currentYearContentColor = VerdeMusgo,
                    selectedYearContainerColor = VerdeMusgo
                )
            )
        }
    }
}

@Composable
fun PagamentoCard(pagamento: Pagamento) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pagamento.clienteNome,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = TextoPrimario
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = pagamento.servicoNome,
                    fontSize = 13.sp,
                    color = TextoSecundario
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatarDataHora(pagamento.dataPagamento),
                    fontSize = 12.sp,
                    color = TextoSecundario
                )
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = VerdeFundo
                ) {
                    Text(
                        text = when (pagamento.metodoPagamento) {
                            "PIX" -> "Pix"
                            "DINHEIRO" -> "Dinheiro"
                            "CARTAO_DEBITO" -> "Cartao Debito"
                            "CARTAO_CREDITO" -> "Cartao Credito"
                            else -> pagamento.metodoPagamento
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = VerdeMusgo,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                text = "R$ ${"%.2f".format(pagamento.valor)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeMusgo
            )
        }
    }
}
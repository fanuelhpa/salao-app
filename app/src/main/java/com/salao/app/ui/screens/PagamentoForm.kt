package com.salao.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salao.app.data.model.Agendamento
import com.salao.app.ui.theme.*
import com.salao.app.viewmodel.PagamentoState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagamentoForm(
    agendamento: Agendamento,
    precoSugerido: Double,
    pagamentoState: PagamentoState,
    onRegistrar: (Double, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var valor by remember { mutableStateOf("%.2f".format(precoSugerido).replace(".", ",")) }
    var metodoPagamento by remember { mutableStateOf("PIX") }
    var metodoDropdownAberto by remember { mutableStateOf(false) }

    // Data de pagamento — começa com hoje
    var dataPagamento by remember { mutableStateOf(LocalDate.now()) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val metodos = listOf("PIX", "DINHEIRO", "CARTAO_DEBITO", "CARTAO_CREDITO")
    val metodoLabels = mapOf(
        "PIX" to "Pix",
        "DINHEIRO" to "Dinheiro",
        "CARTAO_DEBITO" to "Cartão de Débito",
        "CARTAO_CREDITO" to "Cartão de Crédito"
    )

    LaunchedEffect(pagamentoState) {
        if (pagamentoState is PagamentoState.Sucesso) {
            onDismiss()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Registrar Pagamento",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = TextoPrimario
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${agendamento.clienteNome} • ${agendamento.servicoNome}",
            fontSize = 13.sp,
            color = TextoSecundario
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Seletor de data do pagamento
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
                    text = "Data: ${dataPagamento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                    color = TextoPrimario
                )
                Text("📅", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Campo de valor
        OutlinedTextField(
            value = valor,
            onValueChange = { valor = it },
            label = { Text("Valor (R$)") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerdeMusgo,
                focusedLabelColor = VerdeMusgo,
                cursorColor = VerdeMusgo
            ),
            supportingText = {
                Text(
                    "Preço do serviço: R$ ${"%.2f".format(precoSugerido).replace(".", ",")}",
                    fontSize = 12.sp,
                    color = TextoSecundario
                )
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Dropdown de método de pagamento
        ExposedDropdownMenuBox(
            expanded = metodoDropdownAberto,
            onExpandedChange = { metodoDropdownAberto = it }
        ) {
            OutlinedTextField(
                value = metodoLabels[metodoPagamento] ?: metodoPagamento,
                onValueChange = {},
                readOnly = true,
                label = { Text("Método de pagamento") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = metodoDropdownAberto)
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
                expanded = metodoDropdownAberto,
                onDismissRequest = { metodoDropdownAberto = false }
            ) {
                metodos.forEach { metodo ->
                    DropdownMenuItem(
                        text = { Text(metodoLabels[metodo] ?: metodo) },
                        onClick = {
                            metodoPagamento = metodo
                            metodoDropdownAberto = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (pagamentoState is PagamentoState.Erro) {
            Text(
                text = (pagamentoState as PagamentoState.Erro).message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val valorFormatado = valor.replace(",", ".")
                // Formata a data para o formato que a API espera
                val dataFormatada = "${dataPagamento}T00:00:00"
                onRegistrar(
                    valorFormatado.toDoubleOrNull() ?: precoSugerido,
                    metodoPagamento,
                    dataFormatada
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeMusgo),
            enabled = valor.isNotBlank() && pagamentoState !is PagamentoState.Loading
        ) {
            if (pagamentoState is PagamentoState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Branco,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Confirmar pagamento", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    // DatePicker para data do pagamento
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
                            dataPagamento = LocalDate.of(
                                calendar.get(java.util.Calendar.YEAR),
                                calendar.get(java.util.Calendar.MONTH) + 1,
                                calendar.get(java.util.Calendar.DAY_OF_MONTH)
                            )
                        }
                        mostrarDatePicker = false
                    }
                ) { Text("Confirmar", color = VerdeMusgo) }
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
}
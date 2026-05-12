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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagamentoForm(
    agendamento: Agendamento,
    precoSugerido: Double,
    pagamentoState: PagamentoState,
    onRegistrar: (Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    // Começa com o preço do serviço como sugestão
    var valor by remember { mutableStateOf("%.2f".format(precoSugerido).replace(".", ",")) }
    var metodoPagamento by remember { mutableStateOf("PIX") }
    var metodoDropdownAberto by remember { mutableStateOf(false) }

    val metodos = listOf("PIX", "DINHEIRO", "CARTAO_DEBITO", "CARTAO_CREDITO")
    val metodoLabels = mapOf(
        "PIX" to "Pix",
        "DINHEIRO" to "Dinheiro",
        "CARTAO_DEBITO" to "Cartao de Debito",
        "CARTAO_CREDITO" to "Cartao de Credito"
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

        // Campo de valor com preço sugerido preenchido
        OutlinedTextField(
            value = valor,
            onValueChange = { valor = it },
            label = { Text("Valor (R$)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerdeMusgo,
                focusedLabelColor = VerdeMusgo,
                cursorColor = VerdeMusgo
            ),
            // Mostra o preço sugerido como dica abaixo do campo
            supportingText = {
                Text(
                    "Preco do servico: R$ ${"%.2f".format(precoSugerido).replace(".", ",")}",
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
                label = { Text("Metodo de pagamento") },
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
                // Substitui vírgula por ponto antes de converter para Double
                val valorFormatado = valor.replace(",", ".")
                onRegistrar(
                    valorFormatado.toDoubleOrNull() ?: precoSugerido,
                    metodoPagamento
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
}
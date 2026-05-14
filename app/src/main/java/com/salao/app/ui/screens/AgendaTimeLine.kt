package com.salao.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salao.app.data.model.Agendamento
import com.salao.app.ui.theme.*
import androidx.compose.foundation.layout.offset

@Composable
fun AgendaTimeline(
    agendamentos: List<Agendamento>,
    pagos: Map<Long, Double>,
    onAgendamentoClick: (Agendamento) -> Unit
) {
    // Aumentei para 4dp por minuto — 120dp por 30 minutos
    val pixelsPorMinuto = 4f
    val horaInicio = 0
    val horaFim = 24
    val totalMinutos = (horaFim - horaInicio) * 60
    val alturaTotal = (totalMinutos * pixelsPorMinuto).dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(alturaTotal)
    ) {
        for (h in horaInicio until horaFim) {
            for (m in listOf(0, 30)) {
                val totalMinDeste = (h - horaInicio) * 60 + m
                val topOffset = (totalMinDeste * pixelsPorMinuto).dp
                val horaTexto = String.format("%02d:%02d", h, m)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 60.dp)
                        .offset(y = topOffset)
                        .height(0.5.dp)
                        .background(Divisor)
                )

                Text(
                    text = horaTexto,
                    fontSize = 13.sp,
                    color = TextoSecundario,
                    modifier = Modifier
                        .offset(y = topOffset - 8.dp)
                        .width(56.dp)
                        .padding(start = 8.dp, top = 4.dp)
                )
            }
        }

        agendamentos.forEach { agendamento ->
            val horaAgendamento = agendamento.dataHora.substring(11, 16)
            val partes = horaAgendamento.split(":")
            val minutosDoAgendamento = partes[0].toInt() * 60 + partes[1].toInt()
            val minutosInicio = horaInicio * 60
            val minutosFim = horaFim * 60

            if (minutosDoAgendamento in minutosInicio until minutosFim) {
                val offsetTop = ((minutosDoAgendamento - minutosInicio) * pixelsPorMinuto).dp
                val alturaEvento = (agendamento.duracaoMinutos * pixelsPorMinuto).dp

                Box(
                    modifier = Modifier
                        .offset(y = offsetTop)
                        .padding(start = 60.dp, end = 12.dp)
                        .fillMaxWidth()
                ) {
                    EventoCard(
                        agendamento = agendamento,
                        pago = agendamento.id in pagos.keys,
                        valorPago = pagos[agendamento.id],
                        altura = alturaEvento,
                        onClick = { onAgendamentoClick(agendamento) }
                    )
                }
            }
        }
    }
}

@Composable
fun EventoCard(
    agendamento: Agendamento,
    pago: Boolean,
    valorPago: Double?,
    altura: Dp,
    onClick: () -> Unit
) {
    val fundo: Color
    val borda: Color
    val textoNome: Color
    val textoSub: Color
    val statusTexto: String
    val statusFundo: Color
    val mostrarNaoPago: Boolean

    when {
        agendamento.status == "CONCLUIDO" && pago -> {
            fundo = EventoVerdeFundo
            borda = EventoVerdeBorda
            textoNome = EventoVerdeTexto
            textoSub = EventoVerdeTextoSub
            statusTexto = "CONCLUÍDO E PAGO"
            statusFundo = EventoVerdeBorda
            mostrarNaoPago = false
        }
        agendamento.status == "CONCLUIDO" && !pago -> {
            fundo = EventoAmareloFundo
            borda = EventoAmareloBorda
            textoNome = EventoAmareloTexto
            textoSub = EventoAmareloTextoSub
            statusTexto = "CONCLUÍDO"
            statusFundo = EventoAmareloBorda
            mostrarNaoPago = true
        }
        else -> {
            fundo = EventoAzulFundo
            borda = EventoAzulBorda
            textoNome = EventoAzulTexto
            textoSub = EventoAzulTextoSub
            statusTexto = "AGENDADO"
            statusFundo = EventoAzulBorda
            mostrarNaoPago = false
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(altura)
            // Canto direito arredondado, esquerdo reto
            .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 8.dp, bottomEnd = 8.dp, bottomStart = 0.dp))
            .background(fundo)
            .clickable { onClick() }
    ) {
        // Barra lateral colorida
        Box(
            modifier = Modifier
                .width(5.dp)
                .fillMaxHeight()
                .background(borda)
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Coluna esquerda - dados
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = agendamento.clienteNome,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textoNome
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = agendamento.servicoNome,
                    fontSize = 14.sp,
                    color = textoSub
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatarIntervalo(agendamento),
                    fontSize = 14.sp,
                    color = textoSub
                )
            }

            // Coluna direita - status
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusFundo
                ) {
                    Text(
                        text = statusTexto,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Branco,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                if (mostrarNaoPago) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Não pago",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = EventoAmareloBorda
                    )
                }
                // Mostra o valor abaixo do PAGO
                if (pago && valorPago != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "R$ ${"%.2f".format(valorPago).replace(".", ",")}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = EventoVerdeBorda
                    )
                }
            }
        }
    }
}

private fun formatarIntervalo(agendamento: Agendamento): String {
    val horaInicio = agendamento.dataHora.substring(11, 16)
    val partes = horaInicio.split(":")
    val totalMinutos = partes[0].toInt() * 60 + partes[1].toInt() + agendamento.duracaoMinutos
    val horaFim = String.format("%02d:%02d", totalMinutos / 60, totalMinutos % 60)
    return "$horaInicio - $horaFim"
}
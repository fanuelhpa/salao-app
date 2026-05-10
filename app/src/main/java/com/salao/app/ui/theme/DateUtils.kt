package com.salao.app.ui.theme

fun formatarDataHora(dataHora: String): String {
    return try {
        // Divide "2026-07-01T14:50:00" em data e hora
        val partes = dataHora.split("T")
        val data = partes[0]   // "2026-07-01"
        val hora = partes[1].substring(0, 5)  // "14:50"

        // Divide a data em ano, mes e dia
        val (ano, mes, dia) = data.split("-")

        // Retorna no formato brasileiro
        "$dia/$mes/$ano $hora"
    } catch (e: Exception) {
        dataHora // se der erro, retorna o valor original
    }
}
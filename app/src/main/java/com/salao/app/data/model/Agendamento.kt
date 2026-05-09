package com.salao.app.data.model

data class Agendamento(
    val id: Long,
    val clienteId: Long,
    val clienteNome: String,
    val servicoId: Long,
    val servicoNome: String,
    val dataHora: String,
    val status: String,
    val observacoes: String?
)
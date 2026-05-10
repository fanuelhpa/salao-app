package com.salao.app.data.model

data class AgendamentoRequest(
    val clienteId: Long,
    val servicoId: Long,
    val dataHora: String,
    val observacoes: String?
)
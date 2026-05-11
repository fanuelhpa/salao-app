package com.salao.app.data.model

data class PagamentoRequest(
    val agendamentoId: Long,
    val valor: Double,
    val metodoPagamento: String
)
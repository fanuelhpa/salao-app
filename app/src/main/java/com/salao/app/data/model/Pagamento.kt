package com.salao.app.data.model

data class Pagamento(
    val id: Long,
    val agendamentoId: Long,
    val clienteNome: String,
    val servicoNome: String,
    val valor: Double,
    val metodoPagamento: String,
    val dataPagamento: String
)
package com.salao.app.data.model

import java.math.BigDecimal

data class ServicoRequest(
    val nome: String,
    val descricao: String?,
    val duracaoMinutos: Int,
    val preco: BigDecimal
)
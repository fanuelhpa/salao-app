package com.salao.app.data.model

data class Servico(
    val id: Long,
    val nome: String,
    val descricao: String?,
    val duracaoMinutos: Int,
    val preco: Double
)
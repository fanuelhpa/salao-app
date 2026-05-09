package com.salao.app.data.model

data class Cliente(
    val id: Long,
    val nome: String,
    val email: String,
    val telefone: String?,
    val dataCadastro: String?
)
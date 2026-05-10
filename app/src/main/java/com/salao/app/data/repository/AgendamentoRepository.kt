package com.salao.app.data.repository

import com.salao.app.data.model.Agendamento
import com.salao.app.data.model.AgendamentoRequest
import com.salao.app.data.network.RetrofitClient

class AgendamentoRepository(private val token: String) {

    suspend fun listarAgendamentos(): Result<List<Agendamento>> {
        return try {
            val response = RetrofitClient.comToken(token).listarAgendamentos()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun criarAgendamento(request: AgendamentoRequest): Result<Agendamento> {
        return try {
            val response = RetrofitClient.comToken(token).criarAgendamento(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
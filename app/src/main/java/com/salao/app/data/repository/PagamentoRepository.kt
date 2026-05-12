package com.salao.app.data.repository

import com.salao.app.data.model.Pagamento
import com.salao.app.data.model.PagamentoRequest
import com.salao.app.data.network.RetrofitClient
import com.salao.app.data.network.extrairMensagemErro
import com.salao.app.data.network.tratarErro

class PagamentoRepository(private val token: String) {

    suspend fun registrarPagamento(request: PagamentoRequest): Result<Pagamento> {
        return try {
            val response = RetrofitClient.comToken(token).registrarPagamento(request)
            Result.success(response)
        } catch (e: Exception) {
            tratarErro(e, "Erro ao carregar agendamentos.")
        }
    }

    suspend fun buscarPagamentoPorAgendamento(agendamentoId: Long): Result<Pagamento> {
        return try {
            val response = RetrofitClient.comToken(token).buscarPagamentoPorAgendamento(agendamentoId)
            Result.success(response)
        } catch (e: Exception) {
            tratarErro(e, "Erro ao carregar agendamentos.")
        }
    }

    suspend fun listarPagamentos(): Result<List<Pagamento>> {
        return try {
            val response = RetrofitClient.comToken(token).listarPagamentos()
            Result.success(response)
        } catch (e: Exception) {
            tratarErro(e, "Erro ao carregar agendamentos.")
        }
    }

    suspend fun buscarPorPeriodo(inicio: String, fim: String): Result<List<Pagamento>> {
        return try {
            val response = RetrofitClient.comToken(token).buscarPagamentosPorPeriodo(inicio, fim)
            Result.success(response)
        } catch (e: Exception) {
            tratarErro(e, "Erro ao carregar agendamentos.")
        }
    }
}
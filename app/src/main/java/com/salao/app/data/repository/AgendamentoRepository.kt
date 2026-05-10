package com.salao.app.data.repository

import com.salao.app.data.model.Agendamento
import com.salao.app.data.model.AgendamentoRequest
import com.salao.app.data.network.RetrofitClient
import com.salao.app.data.network.extrairMensagemErro

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
            // Extrai a mensagem real do erro da API
            val mensagem = extrairMensagemErro(e, "Erro ao criar agendamento.")
            Result.failure(Exception(mensagem))
        }
    }

    suspend fun atualizarAgendamento(id: Long, request: AgendamentoRequest): Result<Agendamento> {
        return try {
            val response = RetrofitClient.comToken(token).atualizarAgendamento(id, request)
            Result.success(response)
        } catch (e: Exception) {
            val mensagem = extrairMensagemErro(e, "Erro ao atualizar agendamento.")
            Result.failure(Exception(mensagem))
        }
    }

    suspend fun cancelarAgendamento(id: Long): Result<Agendamento> {
        return try {
            val response = RetrofitClient.comToken(token).cancelarAgendamento(id)
            Result.success(response)
        } catch (e: Exception) {
            val mensagem = extrairMensagemErro(e, "Erro ao cancelar agendamento.")
            Result.failure(Exception(mensagem))
        }
    }

    suspend fun concluirAgendamento(id: Long): Result<Agendamento> {
        return try {
            val response = RetrofitClient.comToken(token).concluirAgendamento(id)
            Result.success(response)
        } catch (e: Exception) {
            val mensagem = extrairMensagemErro(e, "Erro ao concluir agendamento.")
            Result.failure(Exception(mensagem))
        }
    }
}
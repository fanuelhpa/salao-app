package com.salao.app.data.repository

import com.salao.app.data.model.Agendamento
import com.salao.app.data.model.AgendamentoRequest
import com.salao.app.data.network.RetrofitClient
import com.salao.app.data.network.extrairMensagemErro
import com.salao.app.data.network.tratarErro

class AgendamentoRepository(private val token: String) {

    suspend fun listarAgendamentos(): Result<List<Agendamento>> {
        return try {
            val response = RetrofitClient.comToken(token).listarAgendamentos()
            Result.success(response)
        } catch (e: Exception) {
            tratarErro(e, "Erro ao carregar agendamentos.")
        }
    }

    suspend fun criarAgendamento(request: AgendamentoRequest): Result<Agendamento> {
        return try {
            val response = RetrofitClient.comToken(token).criarAgendamento(request)
            Result.success(response)
        } catch (e: Exception) {
            tratarErro(e, "Erro ao carregar agendamentos.")
        }
    }

    suspend fun atualizarAgendamento(id: Long, request: AgendamentoRequest): Result<Agendamento> {
        return try {
            val response = RetrofitClient.comToken(token).atualizarAgendamento(id, request)
            Result.success(response)
        } catch (e: Exception) {
            tratarErro(e, "Erro ao carregar agendamentos.")
        }
    }

    suspend fun cancelarAgendamento(id: Long): Result<Agendamento> {
        return try {
            val response = RetrofitClient.comToken(token).cancelarAgendamento(id)
            Result.success(response)
        } catch (e: Exception) {
            tratarErro(e, "Erro ao carregar agendamentos.")
        }
    }

    suspend fun concluirAgendamento(id: Long): Result<Agendamento> {
        return try {
            val response = RetrofitClient.comToken(token).concluirAgendamento(id)
            Result.success(response)
        } catch (e: Exception) {
            tratarErro(e, "Erro ao carregar agendamentos.")
        }
    }

    suspend fun listarAgendamentosPorData(data: String): Result<List<Agendamento>> {
        return try {
            val response = RetrofitClient.comToken(token).listarAgendamentosPorData(data)
            Result.success(response)
        } catch (e: Exception) {
            tratarErro(e, "Erro ao carregar agendamentos.")
        }
    }
}
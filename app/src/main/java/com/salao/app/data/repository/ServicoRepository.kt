package com.salao.app.data.repository

import com.salao.app.data.model.Servico
import com.salao.app.data.model.ServicoRequest
import com.salao.app.data.network.RetrofitClient
import com.salao.app.data.network.extrairMensagemErro
import com.salao.app.data.network.tratarErro

class ServicoRepository(private val token: String) {

    suspend fun listarServicos(): Result<List<Servico>> {
        return try {
            val response = RetrofitClient.comToken(token).listarServicos()
            Result.success(response)
        } catch (e: Exception) {
            tratarErro(e, "Erro ao carregar agendamentos.")
        }
    }

    suspend fun criarServico(request: ServicoRequest): Result<Servico> {
        return try {
            val response = RetrofitClient.comToken(token).criarServico(request)
            Result.success(response)
        } catch (e: Exception) {
            tratarErro(e, "Erro ao carregar agendamentos.")
        }
    }

    suspend fun atualizarServico(id: Long, request: ServicoRequest): Result<Servico> {
        return try {
            val response = RetrofitClient.comToken(token).atualizarServico(id, request)
            Result.success(response)
        } catch (e: Exception) {
            tratarErro(e, "Erro ao carregar agendamentos.")
        }
    }
}
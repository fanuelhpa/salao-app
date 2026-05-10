package com.salao.app.data.repository

import com.salao.app.data.model.Servico
import com.salao.app.data.model.ServicoRequest
import com.salao.app.data.network.RetrofitClient
import com.salao.app.data.network.extrairMensagemErro

class ServicoRepository(private val token: String) {

    suspend fun listarServicos(): Result<List<Servico>> {
        return try {
            val response = RetrofitClient.comToken(token).listarServicos()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun criarServico(request: ServicoRequest): Result<Servico> {
        return try {
            val response = RetrofitClient.comToken(token).criarServico(request)
            Result.success(response)
        } catch (e: Exception) {
            val mensagem = extrairMensagemErro(e, "Erro ao criar servico.")
            Result.failure(Exception(mensagem))
        }
    }

    suspend fun atualizarServico(id: Long, request: ServicoRequest): Result<Servico> {
        return try {
            val response = RetrofitClient.comToken(token).atualizarServico(id, request)
            Result.success(response)
        } catch (e: Exception) {
            val mensagem = extrairMensagemErro(e, "Erro ao atualizar servico.")
            Result.failure(Exception(mensagem))
        }
    }
}
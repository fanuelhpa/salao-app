package com.salao.app.data.repository

import com.salao.app.data.model.Servico
import com.salao.app.data.model.ServicoRequest
import com.salao.app.data.network.RetrofitClient

class ServicoRepository(private val token: String) {

    suspend fun listarServicos(): Result<List<Servico>> {
        return try {
            val response = RetrofitClient.comToken(token).listarServicos()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun atualizarServico(id: Long, request: ServicoRequest): Result<Servico> {
        return try {
            val response = RetrofitClient.comToken(token).atualizarServico(id, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
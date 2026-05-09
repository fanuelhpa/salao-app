package com.salao.app.data.repository

import com.salao.app.data.model.Cliente
import com.salao.app.data.model.ClienteRequest
import com.salao.app.data.network.RetrofitClient

class ClienteRepository(private val token: String) {

    suspend fun listarClientes(): Result<List<Cliente>> {
        return try {
            val response = RetrofitClient.comToken(token).listarClientes()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun criarCliente(request: ClienteRequest): Result<Cliente> {
        return try {
            val response = RetrofitClient.comToken(token).criarCliente(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
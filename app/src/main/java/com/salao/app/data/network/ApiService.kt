package com.salao.app.data.network

import com.salao.app.data.model.Agendamento
import com.salao.app.data.model.Cliente
import com.salao.app.data.model.ClienteRequest
import com.salao.app.data.model.LoginRequest
import com.salao.app.data.model.LoginResponse
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("clientes")
    suspend fun listarClientes(): List<Cliente>

    @POST("clientes")
    suspend fun criarCliente(@Body request: ClienteRequest): Cliente

    @GET("agendamentos")
    suspend fun listarAgendamentos(): List<Agendamento>
}
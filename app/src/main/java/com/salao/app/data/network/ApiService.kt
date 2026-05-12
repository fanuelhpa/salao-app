package com.salao.app.data.network

import com.salao.app.data.model.Agendamento
import com.salao.app.data.model.AgendamentoRequest
import com.salao.app.data.model.Cliente
import com.salao.app.data.model.ClienteRequest
import com.salao.app.data.model.LoginRequest
import com.salao.app.data.model.LoginResponse
import com.salao.app.data.model.Pagamento
import com.salao.app.data.model.PagamentoRequest
import com.salao.app.data.model.Servico
import com.salao.app.data.model.ServicoRequest
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("clientes")
    suspend fun listarClientes(): List<Cliente>

    @POST("clientes")
    suspend fun criarCliente(@Body request: ClienteRequest): Cliente

    @GET("servicos")
    suspend fun listarServicos(): List<Servico>

    @GET("agendamentos")
    suspend fun listarAgendamentos(): List<Agendamento>

    @POST("agendamentos")
    suspend fun criarAgendamento(@Body request: AgendamentoRequest): Agendamento

    @PUT("clientes/{id}")
    suspend fun atualizarCliente(@Path("id") id: Long, @Body request: ClienteRequest): Cliente

    @PATCH("agendamentos/{id}/cancelar")
    suspend fun cancelarAgendamento(@Path("id") id: Long): Agendamento

    @PATCH("agendamentos/{id}/concluir")
    suspend fun concluirAgendamento(@Path("id") id: Long): Agendamento

    @PUT("agendamentos/{id}")
    suspend fun atualizarAgendamento(
        @Path("id") id: Long,
        @Body request: AgendamentoRequest
    ): Agendamento

    @PUT("servicos/{id}")
    suspend fun atualizarServico(@Path("id") id: Long, @Body request: ServicoRequest): Servico

    @POST("servicos")
    suspend fun criarServico(@Body request: ServicoRequest): Servico

    @POST("pagamentos")
    suspend fun registrarPagamento(@Body request: PagamentoRequest): Pagamento

    @GET("pagamentos/agendamento/{agendamentoId}")
    suspend fun buscarPagamentoPorAgendamento(@Path("agendamentoId") agendamentoId: Long): Pagamento

    @GET("pagamentos")
    suspend fun listarPagamentos(): List<Pagamento>

    @GET("pagamentos/periodo")
    suspend fun buscarPagamentosPorPeriodo(
        @Query("inicio") inicio: String,
        @Query("fim") fim: String
    ): List<Pagamento>
}
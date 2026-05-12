package com.salao.app.data.network

import com.salao.app.data.SessionManager
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import retrofit2.HttpException

class UnauthorizedException : Exception("Sessao expirada.")

fun extrairMensagemErro(e: Exception, mensagemPadrao: String): String {
    return try {
        if (e is HttpException) {
            if (e.code() == 401) throw UnauthorizedException()
            val errorBody = e.response()?.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                JSONObject(errorBody).getString("mensagem")
            } else mensagemPadrao
        } else mensagemPadrao
    } catch (ex: UnauthorizedException) {
        throw ex
    } catch (ex: Exception) {
        mensagemPadrao
    }
}

fun <T> tratarErro(e: Exception, mensagemPadrao: String): Result<T> {
    return if (e is HttpException && e.code() == 401) {
        // Limpa o token automaticamente — isso vai disparar o MainActivity
        runBlocking { SessionManager.limparSessao() }
        Result.failure(UnauthorizedException())
    } else {
        Result.failure(Exception(extrairMensagemErro(e, mensagemPadrao)))
    }
}
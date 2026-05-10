package com.salao.app.data.network

import org.json.JSONObject
import retrofit2.HttpException

// Extrai a mensagem de erro do corpo da resposta HTTP
// Quando a API retorna 422, o corpo tem: { "status": 422, "mensagem": "..." }
fun extrairMensagemErro(e: Exception, mensagemPadrao: String): String {
    return try {
        if (e is HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                val json = JSONObject(errorBody)
                json.getString("mensagem")
            } else {
                mensagemPadrao
            }
        } else {
            mensagemPadrao
        }
    } catch (ex: Exception) {
        mensagemPadrao
    }
}
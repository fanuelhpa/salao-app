package com.salao.app.data.repository

import com.salao.app.data.model.LoginRequest
import com.salao.app.data.model.LoginResponse
import com.salao.app.data.network.RetrofitClient

class AuthRepository {

    suspend fun login(email: String, senha: String): Result<LoginResponse> {
        return try {
            val response = RetrofitClient.instance.login(LoginRequest(email, senha))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
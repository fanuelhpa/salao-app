package com.salao.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.salao.app.data.TokenState
import com.salao.app.data.TokenStore
import com.salao.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()
    private val tokenStore = TokenStore(application)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    // Expõe o TokenState para a MainActivity
    val tokenState = tokenStore.tokenState

    fun login(email: String, senha: String) {
        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val result = repository.login(email, senha)

            if (result.isSuccess) {
                val token = result.getOrNull()!!.token
                tokenStore.salvarToken(token)
                _uiState.value = AuthUiState.Success(token)
            } else {
                _uiState.value = AuthUiState.Error("Email ou senha invalidos.")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenStore.limparToken()
            _uiState.value = AuthUiState.Idle
        }
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val token: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
package com.salao.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salao.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    // Estado da tela de login
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, senha: String) {
        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val result = repository.login(email, senha)

            _uiState.value = if (result.isSuccess) {
                AuthUiState.Success(result.getOrNull()!!.token)
            } else {
                AuthUiState.Error("Email ou senha invalidos.")
            }
        }
    }
}

// Estados possiveis da tela de login
sealed class AuthUiState {
    object Idle : AuthUiState()        // estado inicial
    object Loading : AuthUiState()     // carregando
    data class Success(val token: String) : AuthUiState()  // sucesso com token
    data class Error(val message: String) : AuthUiState()  // erro
}
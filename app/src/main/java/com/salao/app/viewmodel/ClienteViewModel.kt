package com.salao.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salao.app.data.model.Cliente
import com.salao.app.data.repository.ClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClienteViewModel(private val token: String) : ViewModel() {

    private val repository = ClienteRepository(token)

    private val _uiState = MutableStateFlow<ClienteUiState>(ClienteUiState.Loading)
    val uiState: StateFlow<ClienteUiState> = _uiState

    init {
        // Carrega os clientes automaticamente ao criar o ViewModel
        carregarClientes()
    }

    fun carregarClientes() {
        _uiState.value = ClienteUiState.Loading

        viewModelScope.launch {
            val result = repository.listarClientes()

            _uiState.value = if (result.isSuccess) {
                ClienteUiState.Success(result.getOrNull()!!)
            } else {
                ClienteUiState.Error("Erro ao carregar clientes.")
            }
        }
    }
}

sealed class ClienteUiState {
    object Loading : ClienteUiState()
    data class Success(val clientes: List<Cliente>) : ClienteUiState()
    data class Error(val message: String) : ClienteUiState()
}
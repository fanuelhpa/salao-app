package com.salao.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salao.app.data.model.Cliente
import com.salao.app.data.model.ClienteRequest
import com.salao.app.data.network.UnauthorizedException
import com.salao.app.data.repository.AgendamentoRepository
import com.salao.app.data.repository.ClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClienteViewModel(
    private val token: String
) : ViewModel() {

    private val repository = ClienteRepository(token)
    private val agendamentoRepository = AgendamentoRepository(token)
    private val _uiState = MutableStateFlow<ClienteUiState>(ClienteUiState.Loading)
    val uiState: StateFlow<ClienteUiState> = _uiState

    // Estado separado para o formulário de cadastro
    private val _formState = MutableStateFlow<FormState>(FormState.Idle)
    val formState: StateFlow<FormState> = _formState

    init {
        carregarClientes()
    }

    fun carregarClientes() {
        _uiState.value = ClienteUiState.Loading
        viewModelScope.launch {
            val result = repository.listarClientes()
            if (result.isSuccess) {
                _uiState.value = ClienteUiState.Success(
                    result.getOrNull()!!.sortedBy { it.nome }
                )
            } else {
                _uiState.value = ClienteUiState.Error("Erro ao carregar clientes.")
            }
        }
    }

    fun criarCliente(nome: String, email: String, telefone: String) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            val result = repository.criarCliente(
                ClienteRequest(nome, email, telefone.ifBlank { null })
            )
            if (result.isSuccess) {
                _formState.value = FormState.Sucesso
                carregarClientes()
            } else {
                _formState.value = FormState.Erro(
                    result.exceptionOrNull()?.message ?: "Erro ao criar cliente."
                )
            }
        }
    }

    fun atualizarCliente(id: Long, nome: String, email: String, telefone: String) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            val result = repository.atualizarCliente(
                id, ClienteRequest(nome, email, telefone.ifBlank { null })
            )
            if (result.isSuccess) {
                _formState.value = FormState.Sucesso
                carregarClientes()
            } else {
                _formState.value = FormState.Erro(
                    result.exceptionOrNull()?.message ?: "Erro ao atualizar cliente."
                )
            }
        }
    }

    fun resetFormState() {
        _formState.value = FormState.Idle
    }
}

sealed class ClienteUiState {
    object Loading : ClienteUiState()
    data class Success(val clientes: List<Cliente>) : ClienteUiState()
    data class Error(val message: String) : ClienteUiState()
}

// Estado do formulário de cadastro
sealed class FormState {
    object Idle : FormState()       // inicial
    object Loading : FormState()    // salvando
    object Sucesso : FormState()    // salvo com sucesso
    data class Erro(val message: String) : FormState()  // erro ao salvar
}
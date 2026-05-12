package com.salao.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salao.app.data.model.Servico
import com.salao.app.data.model.ServicoRequest
import com.salao.app.data.network.UnauthorizedException
import com.salao.app.data.repository.ServicoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServicoViewModel(
    private val token: String
) : ViewModel() {

    private val repository = ServicoRepository(token)

    private val _uiState = MutableStateFlow<ServicoUiState>(ServicoUiState.Loading)
    val uiState: StateFlow<ServicoUiState> = _uiState

    private val _formState = MutableStateFlow<FormState>(FormState.Idle)
    val formState: StateFlow<FormState> = _formState

    init { carregarServicos() }

    fun carregarServicos() {
        _uiState.value = ServicoUiState.Loading
        viewModelScope.launch {
            val result = repository.listarServicos()
            if (result.isSuccess) {
                _uiState.value = ServicoUiState.Success(
                    result.getOrNull()!!.sortedBy { it.nome }
                )
            } else {
                _uiState.value = ServicoUiState.Error("Erro ao carregar servicos.")
            }
        }
    }

    fun criarServico(nome: String, descricao: String, duracaoMinutos: Int, preco: Double) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            val result = repository.criarServico(
                ServicoRequest(nome, descricao.ifBlank { null }, duracaoMinutos, preco.toBigDecimal())
            )
            if (result.isSuccess) {
                _formState.value = FormState.Sucesso
                carregarServicos()
            } else {
                _formState.value = FormState.Erro(
                    result.exceptionOrNull()?.message ?: "Erro ao criar servico."
                )
            }
        }
    }

    fun atualizarServico(id: Long, nome: String, descricao: String, duracaoMinutos: Int, preco: Double) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            val result = repository.atualizarServico(
                id,
                ServicoRequest(nome, descricao.ifBlank { null }, duracaoMinutos, preco.toBigDecimal())
            )
            if (result.isSuccess) {
                _formState.value = FormState.Sucesso
                carregarServicos()
            } else {
                _formState.value = FormState.Erro(
                    result.exceptionOrNull()?.message ?: "Erro ao atualizar servico."
                )
            }
        }
    }

    fun resetFormState() { _formState.value = FormState.Idle }
}

sealed class ServicoUiState {
    object Loading : ServicoUiState()
    data class Success(val servicos: List<Servico>) : ServicoUiState()
    data class Error(val message: String) : ServicoUiState()
}
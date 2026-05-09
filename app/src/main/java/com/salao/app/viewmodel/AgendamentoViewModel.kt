package com.salao.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salao.app.data.model.Agendamento
import com.salao.app.data.repository.AgendamentoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AgendamentoViewModel(private val token: String) : ViewModel() {

    private val repository = AgendamentoRepository(token)

    private val _uiState = MutableStateFlow<AgendamentoUiState>(AgendamentoUiState.Loading)
    val uiState: StateFlow<AgendamentoUiState> = _uiState

    init {
        carregarAgendamentos()
    }

    fun carregarAgendamentos() {
        _uiState.value = AgendamentoUiState.Loading

        viewModelScope.launch {
            val result = repository.listarAgendamentos()

            _uiState.value = if (result.isSuccess) {
                AgendamentoUiState.Success(result.getOrNull()!!)
            } else {
                AgendamentoUiState.Error("Erro ao carregar agendamentos.")
            }
        }
    }
}

sealed class AgendamentoUiState {
    object Loading : AgendamentoUiState()
    data class Success(val agendamentos: List<Agendamento>) : AgendamentoUiState()
    data class Error(val message: String) : AgendamentoUiState()
}
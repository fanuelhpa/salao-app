package com.salao.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salao.app.data.model.Agendamento
import com.salao.app.data.model.AgendamentoRequest
import com.salao.app.data.model.Cliente
import com.salao.app.data.model.Servico
import com.salao.app.data.repository.AgendamentoRepository
import com.salao.app.data.repository.ClienteRepository
import com.salao.app.data.repository.ServicoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AgendamentoViewModel(private val token: String) : ViewModel() {

    private val agendamentoRepository = AgendamentoRepository(token)
    private val clienteRepository = ClienteRepository(token)
    private val servicoRepository = ServicoRepository(token)

    private val _uiState = MutableStateFlow<AgendamentoUiState>(AgendamentoUiState.Loading)
    val uiState: StateFlow<AgendamentoUiState> = _uiState

    private val _formState = MutableStateFlow<FormState>(FormState.Idle)
    val formState: StateFlow<FormState> = _formState

    private val _clientes = MutableStateFlow<List<Cliente>>(emptyList())
    val clientes: StateFlow<List<Cliente>> = _clientes

    private val _servicos = MutableStateFlow<List<Servico>>(emptyList())
    val servicos: StateFlow<List<Servico>> = _servicos

    init {
        carregarAgendamentos()
        carregarClientesEServicos()
    }

    fun carregarAgendamentos() {
        _uiState.value = AgendamentoUiState.Loading
        viewModelScope.launch {
            val result = agendamentoRepository.listarAgendamentos()
            _uiState.value = if (result.isSuccess) {
                AgendamentoUiState.Success(result.getOrNull()!!)
            } else {
                AgendamentoUiState.Error("Erro ao carregar agendamentos.")
            }
        }
    }

    private fun carregarClientesEServicos() {
        viewModelScope.launch {
            val result = clienteRepository.listarClientes()
            if (result.isSuccess) _clientes.value = result.getOrNull()!!
        }
        viewModelScope.launch {
            val result = servicoRepository.listarServicos()
            if (result.isSuccess) _servicos.value = result.getOrNull()!!
        }
    }

    fun criarAgendamento(
        clienteId: Long,
        servicoId: Long,
        dataHora: String,
        observacoes: String
    ) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            val result = agendamentoRepository.criarAgendamento(
                AgendamentoRequest(
                    clienteId = clienteId,
                    servicoId = servicoId,
                    dataHora = dataHora,
                    observacoes = observacoes.ifBlank { null }
                )
            )
            if (result.isSuccess) {
                _formState.value = FormState.Sucesso
                carregarAgendamentos()
            } else {
                // Usa a mensagem real do erro em vez de uma mensagem genérica
                _formState.value = FormState.Erro(
                    result.exceptionOrNull()?.message ?: "Erro ao criar agendamento."
                )
            }
        }
    }

    fun cancelarAgendamento(id: Long) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            val result = agendamentoRepository.cancelarAgendamento(id)
            if (result.isSuccess) {
                _formState.value = FormState.Sucesso
                carregarAgendamentos()
            } else {
                _formState.value = FormState.Erro("Erro ao cancelar agendamento.")
            }
        }
    }

    fun atualizarAgendamento(id: Long, servicoId: Long, dataHora: String) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            val agendamentoAtual = (_uiState.value as? AgendamentoUiState.Success)
                ?.agendamentos?.find { it.id == id }

            val result = agendamentoRepository.atualizarAgendamento(
                id,
                AgendamentoRequest(
                    clienteId = agendamentoAtual?.clienteId ?: 0,
                    servicoId = servicoId,
                    dataHora = dataHora,
                    observacoes = null
                )
            )
            if (result.isSuccess) {
                _formState.value = FormState.Sucesso
                carregarAgendamentos()
            } else {
                _formState.value = FormState.Erro(
                    result.exceptionOrNull()?.message ?: "Erro ao atualizar agendamento."
                )
            }
        }
    }

    fun concluirAgendamento(id: Long) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            val result = agendamentoRepository.concluirAgendamento(id)
            if (result.isSuccess) {
                _formState.value = FormState.Sucesso
                carregarAgendamentos()
            } else {
                _formState.value = FormState.Erro("Erro ao concluir agendamento.")
            }
        }
    }

    fun resetFormState() {
        _formState.value = FormState.Idle
    }
}

sealed class AgendamentoUiState {
    object Loading : AgendamentoUiState()
    data class Success(val agendamentos: List<Agendamento>) : AgendamentoUiState()
    data class Error(val message: String) : AgendamentoUiState()
}
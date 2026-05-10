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

    // Listas para popular os dropdowns do formulário
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

    // Carrega clientes e serviços em paralelo para popular os dropdowns
    private fun carregarClientesEServicos() {
        viewModelScope.launch {
            val clientesResult = clienteRepository.listarClientes()
            if (clientesResult.isSuccess) {
                _clientes.value = clientesResult.getOrNull()!!
            }
        }
        viewModelScope.launch {
            val servicosResult = servicoRepository.listarServicos()
            if (servicosResult.isSuccess) {
                _servicos.value = servicosResult.getOrNull()!!
            }
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
                _formState.value = FormState.Erro("Erro ao criar agendamento.")
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
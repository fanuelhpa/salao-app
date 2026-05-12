package com.salao.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salao.app.data.model.Agendamento
import com.salao.app.data.model.AgendamentoRequest
import com.salao.app.data.model.Cliente
import com.salao.app.data.model.Servico
import com.salao.app.data.repository.AgendamentoRepository
import com.salao.app.data.repository.ClienteRepository
import com.salao.app.data.repository.PagamentoRepository
import com.salao.app.data.repository.ServicoRepository
import com.salao.app.data.model.PagamentoRequest
import com.salao.app.data.network.UnauthorizedException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class FiltroStatus { AGENDADO, CONCLUIDO, CANCELADO }

class AgendamentoViewModel(
    private val token: String
) : ViewModel() {

    private val agendamentoRepository = AgendamentoRepository(token)
    private val clienteRepository = ClienteRepository(token)
    private val servicoRepository = ServicoRepository(token)
    private val pagamentoRepository = PagamentoRepository(token)

    private val _todosAgendamentos = MutableStateFlow<List<Agendamento>>(emptyList())
    private val _dataSelecionada = MutableStateFlow(LocalDate.now())
    val dataSelecionada: StateFlow<LocalDate> = _dataSelecionada

    private val _filtroStatus = MutableStateFlow(FiltroStatus.AGENDADO)
    val filtroStatus: StateFlow<FiltroStatus> = _filtroStatus

    private val _carregando = MutableStateFlow(true)
    val carregando: StateFlow<Boolean> = _carregando

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    val agendamentosFiltrados: StateFlow<List<Agendamento>> = combine(
        _todosAgendamentos,
        _dataSelecionada,
        _filtroStatus
    ) { agendamentos, data, filtro ->
        val dataFormatada = data.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        agendamentos.filter { agendamento ->
            agendamento.dataHora.startsWith(dataFormatada) &&
                    agendamento.status == filtro.name
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _formState = MutableStateFlow<FormState>(FormState.Idle)
    val formState: StateFlow<FormState> = _formState

    private val _clientes = MutableStateFlow<List<Cliente>>(emptyList())
    val clientes: StateFlow<List<Cliente>> = _clientes

    private val _servicos = MutableStateFlow<List<Servico>>(emptyList())
    val servicos: StateFlow<List<Servico>> = _servicos

    private val _agendamentosPagos = MutableStateFlow<Set<Long>>(emptySet())
    val agendamentosPagos: StateFlow<Set<Long>> = _agendamentosPagos

    private val _pagamentoState = MutableStateFlow<PagamentoState>(PagamentoState.Idle)
    val pagamentoState: StateFlow<PagamentoState> = _pagamentoState

    init {
        carregarAgendamentos()
        carregarClientesEServicos()
    }

    fun carregarAgendamentos() {
        _carregando.value = true
        _erro.value = null
        viewModelScope.launch {
            val result = agendamentoRepository.listarAgendamentos()
            if (result.isSuccess) {
                _todosAgendamentos.value = result.getOrNull()!!
            } else {
                _erro.value = "Erro ao carregar agendamentos."
            }
            _carregando.value = false
        }
        viewModelScope.launch {
            val result = pagamentoRepository.listarPagamentos()
            if (result.isSuccess) {
                _agendamentosPagos.value = result.getOrNull()!!
                    .map { it.agendamentoId }
                    .toSet()
            }
        }
    }

    fun selecionarData(data: LocalDate) { _dataSelecionada.value = data }
    fun selecionarFiltro(filtro: FiltroStatus) { _filtroStatus.value = filtro }

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

    fun criarAgendamento(clienteId: Long, servicoId: Long, dataHora: String, observacoes: String) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            val result = agendamentoRepository.criarAgendamento(
                AgendamentoRequest(clienteId, servicoId, dataHora, observacoes.ifBlank { null })
            )
            if (result.isSuccess) {
                _formState.value = FormState.Sucesso
                carregarAgendamentos()
            } else {
                _formState.value = FormState.Erro(
                    result.exceptionOrNull()?.message ?: "Erro ao criar agendamento."
                )
            }
        }
    }

    fun atualizarAgendamento(id: Long, servicoId: Long, dataHora: String) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            val agendamentoAtual = _todosAgendamentos.value.find { it.id == id }
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

    fun cancelarAgendamento(id: Long) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            val result = agendamentoRepository.cancelarAgendamento(id)
            if (result.isSuccess) {
                _formState.value = FormState.Sucesso
                carregarAgendamentos()
            } else {
                _formState.value = FormState.Erro(
                    result.exceptionOrNull()?.message ?: "Erro ao cancelar agendamento."
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
                _formState.value = FormState.Erro(
                    result.exceptionOrNull()?.message ?: "Erro ao concluir agendamento."
                )
            }
        }
    }

    fun registrarPagamento(agendamentoId: Long, valor: Double, metodoPagamento: String) {
        _pagamentoState.value = PagamentoState.Loading
        viewModelScope.launch {
            val result = pagamentoRepository.registrarPagamento(
                PagamentoRequest(agendamentoId, valor, metodoPagamento)
            )
            if (result.isSuccess) {
                _pagamentoState.value = PagamentoState.Sucesso
                _agendamentosPagos.value = _agendamentosPagos.value + agendamentoId
            } else {
                _pagamentoState.value = PagamentoState.Erro(
                    result.exceptionOrNull()?.message ?: "Erro ao registrar pagamento."
                )
            }
        }
    }

    fun resetFormState() { _formState.value = FormState.Idle }
    fun resetPagamentoState() { _pagamentoState.value = PagamentoState.Idle }
}

sealed class AgendamentoUiState {
    object Loading : AgendamentoUiState()
    data class Success(val agendamentos: List<Agendamento>) : AgendamentoUiState()
    data class Error(val message: String) : AgendamentoUiState()
}

sealed class PagamentoState {
    object Idle : PagamentoState()
    object Loading : PagamentoState()
    object Sucesso : PagamentoState()
    data class Erro(val message: String) : PagamentoState()
}
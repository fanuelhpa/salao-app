package com.salao.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salao.app.data.model.Agendamento
import com.salao.app.data.model.AgendamentoRequest
import com.salao.app.data.model.Cliente
import com.salao.app.data.model.PagamentoRequest
import com.salao.app.data.model.Servico
import com.salao.app.data.repository.AgendamentoRepository
import com.salao.app.data.repository.ClienteRepository
import com.salao.app.data.repository.PagamentoRepository
import com.salao.app.data.repository.ServicoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class FiltroStatus { AGENDADO, PENDENTES }

class AgendamentoViewModel(private val token: String) : ViewModel() {
    
    // Cache por filtro
    private val cacheAgendamentos = mutableMapOf<FiltroStatus, List<Agendamento>>()
    private val cacheDatas = mutableMapOf<FiltroStatus, LocalDate>()

    private val agendamentoRepository = AgendamentoRepository(token)
    private val clienteRepository = ClienteRepository(token)
    private val servicoRepository = ServicoRepository(token)
    private val pagamentoRepository = PagamentoRepository(token)

    private val _agendamentos = MutableStateFlow<List<Agendamento>>(emptyList())
    val agendamentos: StateFlow<List<Agendamento>> = _agendamentos

    private val _dataSelecionada = MutableStateFlow(LocalDate.now())
    val dataSelecionada: StateFlow<LocalDate> = _dataSelecionada

    private val _filtroStatus = MutableStateFlow(FiltroStatus.AGENDADO)
    val filtroStatus: StateFlow<FiltroStatus> = _filtroStatus

    private val _carregando = MutableStateFlow(true)
    val carregando: StateFlow<Boolean> = _carregando

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _formState = MutableStateFlow<FormState>(FormState.Idle)
    val formState: StateFlow<FormState> = _formState

    private val _cancelamentoState = MutableStateFlow<FormState>(FormState.Idle)
    val cancelamentoState: StateFlow<FormState> = _cancelamentoState

    private val _clientes = MutableStateFlow<List<Cliente>>(emptyList())
    val clientes: StateFlow<List<Cliente>> = _clientes

    private val _servicos = MutableStateFlow<List<Servico>>(emptyList())
    val servicos: StateFlow<List<Servico>> = _servicos

    private val _agendamentosPagos = MutableStateFlow<Map<Long, Double>>(emptyMap())
    val agendamentosPagos: StateFlow<Map<Long, Double>> = _agendamentosPagos

    private val _pagamentoState = MutableStateFlow<PagamentoState>(PagamentoState.Idle)
    val pagamentoState: StateFlow<PagamentoState> = _pagamentoState

    // Agendamentos filtrados por status — computado localmente
    val agendamentosFiltrados: StateFlow<List<Agendamento>>
        get() = _agendamentos

    init {
        carregarAgendamentosDoDia()
        carregarPagamentos()
        carregarClientesEServicos()
    }

    // Carrega agendamentos do dia selecionado
    fun carregarAgendamentosDoDia(forcar: Boolean = false) {
        val dataAtual = _dataSelecionada.value
        val filtroAtual = _filtroStatus.value

        // Verifica se tem cache válido para esse filtro
        if (!forcar &&
            cacheDatas[filtroAtual] == dataAtual &&
            cacheAgendamentos[filtroAtual] != null) {
            _agendamentos.value = cacheAgendamentos[filtroAtual]!!
            return
        }

        _carregando.value = true
        _erro.value = null
        viewModelScope.launch {
            val dataFormatada = dataAtual.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            if (filtroAtual == FiltroStatus.PENDENTES) {
                val result = agendamentoRepository.listarAgendamentos()
                if (result.isSuccess) {
                    val hoje = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val lista = result.getOrNull()!!.filter { agendamento ->
                        val dataAgendamento = agendamento.dataHora.substring(0, 10)
                        dataAgendamento < hoje && (
                                agendamento.status == "AGENDADO" ||
                                        (agendamento.status == "CONCLUIDO" && agendamento.id !in _agendamentosPagos.value.keys)
                                )
                    }
                    _agendamentos.value = lista
                    cacheAgendamentos[filtroAtual] = lista
                    cacheDatas[filtroAtual] = dataAtual
                } else {
                    _erro.value = "Erro ao carregar agendamentos."
                }
            } else {
                val result = agendamentoRepository.listarAgendamentosPorData(dataFormatada)
                if (result.isSuccess) {
                    val lista = result.getOrNull()!!
                        .filter { it.status == "AGENDADO" || it.status == "CONCLUIDO" }
                        .sortedBy { it.dataHora }
                    _agendamentos.value = lista
                    cacheAgendamentos[filtroAtual] = lista
                    cacheDatas[filtroAtual] = dataAtual
                } else {
                    _erro.value = "Erro ao carregar agendamentos."
                }
            }
            _carregando.value = false
        }
    }

    fun selecionarData(data: LocalDate) {
        _dataSelecionada.value = data
        carregarAgendamentosDoDia(forcar = true)
    }

    fun selecionarFiltro(filtro: FiltroStatus) {
        if (_filtroStatus.value == filtro) return
        _filtroStatus.value = filtro
        carregarAgendamentosDoDia()
    }

    fun carregarAgendamentos() {
        carregarAgendamentosDoDia(forcar = true)
    }

    private fun carregarPagamentos() {
        viewModelScope.launch {
            val result = pagamentoRepository.listarPagamentos()
            if (result.isSuccess) {
                _agendamentosPagos.value = result.getOrNull()!!
                    .associate { it.agendamentoId to it.valor }
            }
        }
    }

    fun carregarClientesEServicos() {
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
                cacheAgendamentos.clear()
                cacheDatas.clear()
                carregarAgendamentosDoDia()
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
            val agendamentoAtual = _agendamentos.value.find { it.id == id }
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
                cacheAgendamentos.clear()
                cacheDatas.clear()
                carregarAgendamentosDoDia()
            } else {
                _formState.value = FormState.Erro(
                    result.exceptionOrNull()?.message ?: "Erro ao atualizar agendamento."
                )
            }
        }
    }

    fun cancelarAgendamento(id: Long) {
        _cancelamentoState.value = FormState.Loading
        viewModelScope.launch {
            val result = agendamentoRepository.cancelarAgendamento(id)
            if (result.isSuccess) {
                _cancelamentoState.value = FormState.Sucesso
                cacheAgendamentos.clear()
                cacheDatas.clear()
                carregarAgendamentosDoDia()
            } else {
                _cancelamentoState.value = FormState.Erro(
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
                cacheAgendamentos.clear()
                cacheDatas.clear()
                carregarAgendamentosDoDia()
            } else {
                _formState.value = FormState.Erro(
                    result.exceptionOrNull()?.message ?: "Erro ao concluir agendamento."
                )
            }
        }
    }

    fun registrarPagamento(agendamentoId: Long, valor: Double, metodoPagamento: String, dataPagamento: String) {
        _pagamentoState.value = PagamentoState.Loading
        viewModelScope.launch {
            val result = pagamentoRepository.registrarPagamento(
                PagamentoRequest(agendamentoId, valor, metodoPagamento, dataPagamento)
            )
            if (result.isSuccess) {
                _pagamentoState.value = PagamentoState.Sucesso
                _agendamentosPagos.value = _agendamentosPagos.value + (agendamentoId to valor)
                cacheAgendamentos.clear()
                cacheDatas.clear()
                carregarAgendamentosDoDia()
            } else {
                _pagamentoState.value = PagamentoState.Erro(
                    result.exceptionOrNull()?.message ?: "Erro ao registrar pagamento."
                )
            }
        }
    }

    fun resetFormState() { _formState.value = FormState.Idle }
    fun resetCancelamentoState() { _cancelamentoState.value = FormState.Idle }
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
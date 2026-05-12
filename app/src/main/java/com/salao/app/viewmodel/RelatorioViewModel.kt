package com.salao.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salao.app.data.model.Pagamento
import com.salao.app.data.network.UnauthorizedException
import com.salao.app.data.repository.PagamentoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class TipoRelatorio { DIA, MES, PERIODO, ANO }

class RelatorioViewModel(
    private val token: String
) : ViewModel() {

    private val pagamentoRepository = PagamentoRepository(token)

    private val _uiState = MutableStateFlow<RelatorioUiState>(RelatorioUiState.Idle)
    val uiState: StateFlow<RelatorioUiState> = _uiState

    private val _tipoSelecionado = MutableStateFlow(TipoRelatorio.DIA)
    val tipoSelecionado: StateFlow<TipoRelatorio> = _tipoSelecionado

    fun selecionarTipo(tipo: TipoRelatorio) {
        _tipoSelecionado.value = tipo
        _uiState.value = RelatorioUiState.Idle
    }

    fun buscarPorDia(data: LocalDate) {
        buscar(data.atStartOfDay().toString(), data.atTime(23, 59, 59).toString())
    }

    fun buscarPorMes(ano: Int, mes: Int) {
        val inicio = LocalDate.of(ano, mes, 1).atStartOfDay()
        val fim = LocalDate.of(ano, mes, 1)
            .withDayOfMonth(LocalDate.of(ano, mes, 1).lengthOfMonth())
            .atTime(23, 59, 59)
        buscar(inicio.toString(), fim.toString())
    }

    fun buscarPorPeriodo(dataInicio: LocalDate, dataFim: LocalDate) {
        buscar(dataInicio.atStartOfDay().toString(), dataFim.atTime(23, 59, 59).toString())
    }

    fun buscarPorAno(ano: Int) {
        buscar(
            LocalDate.of(ano, 1, 1).atStartOfDay().toString(),
            LocalDate.of(ano, 12, 31).atTime(23, 59, 59).toString()
        )
    }

    private fun buscar(inicio: String, fim: String) {
        _uiState.value = RelatorioUiState.Loading
        viewModelScope.launch {
            val result = pagamentoRepository.buscarPorPeriodo(inicio, fim)
            if (result.isSuccess) {
                _uiState.value = RelatorioUiState.Success(result.getOrNull()!!)
            } else {
                _uiState.value = RelatorioUiState.Error(
                    result.exceptionOrNull()?.message ?: "Erro ao buscar relatorio."
                )
            }
        }
    }
}

sealed class RelatorioUiState {
    object Idle : RelatorioUiState()
    object Loading : RelatorioUiState()
    data class Success(val pagamentos: List<Pagamento>) : RelatorioUiState()
    data class Error(val message: String) : RelatorioUiState()
}
package com.example.myapplication.viewmodel

import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.localstorage.PreferenciasApp
import com.example.myapplication.model.Categoria
import com.example.myapplication.model.Transacao

class AppViewModel(
    private val prefs: PreferenciasApp
) : ViewModel() {

    var isUserLoggedIn = mutableStateOf(prefs.isLoggedIn())
        private set

    // Listas em memória (Sem banco de dados)
    var allTransacoes = mutableStateListOf<Transacao>()
        private set

    var allCategorias = mutableStateListOf<Categoria>()
        private set

    // Totais calculados
    var totalReceitasEfetivadas = mutableDoubleStateOf(0.0)
        private set
    var totalReceitasPrevistas = mutableDoubleStateOf(0.0)
        private set
    var totalDespesasEfetivadas = mutableDoubleStateOf(0.0)
        private set
    var totalDespesasPrevistas = mutableDoubleStateOf(0.0)
        private set

    private var nextTransacaoId = 1L
    private var nextCategoriaId = 1L

    init {
        // Inicializa as categorias padrão em memória
        inicializarCategoriasPadrao()
    }

    fun login() {
        prefs.setLoggedIn(true)
        isUserLoggedIn.value = true
    }

    fun logout() {
        prefs.setLoggedIn(false)
        isUserLoggedIn.value = false
    }

    private fun inicializarCategoriasPadrao() {
        val padroes = listOf(
            Triple("Alimentação", "🍔", "#FF5722"),
            Triple("Transporte", "🚗", "#2196F3"),
            Triple("Lazer", "🎮", "#4CAF50"),
            Triple("Moradia", "🏠", "#9C27B0")
        )
        for (item in padroes) {
            allCategorias.add(
                Categoria(
                    id = nextCategoriaId++,
                    nome = item.first,
                    iconeNome = item.second,
                    corHex = item.third,
                    limiteMensal = 500.0
                )
            )
        }
    }

    fun addTransacao(descricao: String, valor: Double, tipo: String, categoriaId: Long, efetivado: Boolean) {
        val novaTransacao = Transacao(
            id = nextTransacaoId++,
            descricao = descricao,
            valor = valor,
            tipo = tipo,
            dataInMillis = System.currentTimeMillis(),
            categoriaId = categoriaId,
            efetivado = efetivado
        )
        allTransacoes.add(0, novaTransacao) // Adiciona no início da lista
        recalcularTotais()
    }

    fun deleteTransacao(transacao: Transacao) {
        allTransacoes.remove(transacao)
        recalcularTotais()
    }

    fun updateCategoriaLimite(categoria: Categoria, novoLimite: Double) {
        val index = allCategorias.indexOfFirst { it.id == categoria.id }
        if (index != -1) {
            allCategorias[index] = categoria.copy(limiteMensal = novoLimite)
        }
    }

    private fun recalcularTotais() {
        var recEfetivadas = 0.0
        var recPrevistas = 0.0
        var despEfetivadas = 0.0
        var despPrevistas = 0.0

        for (t in allTransacoes) {
            if (t.tipo == "RECEITA") {
                if (t.efetivado) recEfetivadas += t.valor else recPrevistas += t.valor
            } else if (t.tipo == "DESPESA") {
                if (t.efetivado) despEfetivadas += t.valor else despPrevistas += t.valor
            }
        }

        totalReceitasEfetivadas.doubleValue = recEfetivadas
        totalReceitasPrevistas.doubleValue = recPrevistas
        totalDespesasEfetivadas.doubleValue = despEfetivadas
        totalDespesasPrevistas.doubleValue = despPrevistas
    }

    fun getGastoDaCategoria(categoriaId: Long): Double {
        var gastoTotal = 0.0
        for (t in allTransacoes) {
            if (t.categoriaId == categoriaId && t.tipo == "DESPESA") {
                gastoTotal += t.valor
            }
        }
        return gastoTotal
    }

    fun getRecentTransacoes(): List<Transacao> {
        return allTransacoes.take(5)
    }
}

class AppViewModelFactory(
    private val prefs: PreferenciasApp
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

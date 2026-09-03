package com.example.myapplication.viewmodel

import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.localstorage.PreferenciasApp
import com.example.myapplication.model.CategoriaEntity
import com.example.myapplication.model.TransacaoEntity
import com.example.myapplication.repository.AppRepository
import kotlinx.coroutines.launch

class AppViewModel(
    private val repository: AppRepository,
    private val prefs: PreferenciasApp
) : ViewModel() {

    var isUserLoggedIn = mutableStateOf(prefs.isLoggedIn())
        private set

    var allTransacoes = mutableStateListOf<TransacaoEntity>()
        private set

    var recentTransacoes = mutableStateListOf<TransacaoEntity>()
        private set

    var allCategorias = mutableStateListOf<CategoriaEntity>()
        private set

    var totalReceitasEfetivadas = mutableDoubleStateOf(0.0)
        private set
    var totalReceitasPrevistas = mutableDoubleStateOf(0.0)
        private set
    var totalDespesasEfetivadas = mutableDoubleStateOf(0.0)
        private set
    var totalDespesasPrevistas = mutableDoubleStateOf(0.0)
        private set

    init {
        loadData()
    }

    fun login() {
        prefs.setLoggedIn(true)
        isUserLoggedIn.value = true
    }

    fun logout() {
        prefs.setLoggedIn(false)
        isUserLoggedIn.value = false
    }

    fun loadData() {
        viewModelScope.launch {
            val transacoesDoBanco = repository.getAllTransacoes()
            val categoriasDoBanco = repository.getAllCategorias()

            allTransacoes.clear()
            allTransacoes.addAll(transacoesDoBanco)

            recentTransacoes.clear()
            recentTransacoes.addAll(repository.getRecentTransacoes())

            allCategorias.clear()
            allCategorias.addAll(categoriasDoBanco)

            if (categoriasDoBanco.isEmpty()) {
                initializeDefaultCategories()
            }

            calcularTotais(transacoesDoBanco)
        }
    }

    private fun calcularTotais(transacoes: List<TransacaoEntity>) {
        var recEfetivadas = 0.0
        var recPrevistas = 0.0
        var despEfetivadas = 0.0
        var despPrevistas = 0.0

        for (t in transacoes) {
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

    private suspend fun initializeDefaultCategories() {
        val defaults = listOf(
            Triple("Alimentação", "🍔", "#FF5722"),
            Triple("Transporte", "🚗", "#2196F3"),
            Triple("Lazer", "🎮", "#4CAF50")
        )
        for (item in defaults) {
            repository.insertCategoria(
                CategoriaEntity(nome = item.first, iconeNome = item.second, corHex = item.third, limiteMensal = 500.0)
            )
        }
        val novasCategorias = repository.getAllCategorias()
        allCategorias.clear()
        allCategorias.addAll(novasCategorias)
    }

    fun addTransacao(descricao: String, valor: Double, tipo: String, categoriaId: Long, efetivado: Boolean) {
        viewModelScope.launch {
            val novaTransacao = TransacaoEntity(
                descricao = descricao,
                valor = valor,
                tipo = tipo,
                dataInMillis = System.currentTimeMillis(),
                categoriaId = categoriaId,
                efetivado = efetivado
            )
            repository.insertTransacao(novaTransacao)
            loadData() 
        }
    }

    fun deleteTransacao(transacao: TransacaoEntity) {
        viewModelScope.launch {
            repository.deleteTransacao(transacao)
            loadData()
        }
    }

    fun updateCategoriaLimite(categoria: CategoriaEntity, novoLimite: Double) {
        viewModelScope.launch {
            val categoriaAtualizada = categoria.copy(limiteMensal = novoLimite)
            repository.updateCategoria(categoriaAtualizada)
            loadData()
        }
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
}

class AppViewModelFactory(
    private val repository: AppRepository,
    private val prefs: PreferenciasApp
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

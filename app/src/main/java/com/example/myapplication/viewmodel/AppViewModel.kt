package com.example.myapplication.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.CotacoesResponse
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.database.CategoriaDAO
import com.example.myapplication.database.TransacaoDAO
import com.example.myapplication.database.UsuarioDAO
import com.example.myapplication.localstorage.PreferenciasApp
import com.example.myapplication.model.Categoria
import com.example.myapplication.model.Transacao
import com.example.myapplication.model.Usuario
import kotlinx.coroutines.launch

class AppViewModel(
    private val prefs: PreferenciasApp,
    private val usuarioDAO: UsuarioDAO,
    private val categoriaDAO: CategoriaDAO,
    private val transacaoDAO: TransacaoDAO
) : ViewModel() {

    // ---------- Sessão ----------
    var isUserLoggedIn = mutableStateOf(prefs.isLoggedIn())
        private set

    var nomeUsuario = mutableStateOf(prefs.getNomeUsuario())
        private set

    var carregando by mutableStateOf(false)
        private set

    var mensagem by mutableStateOf<String?>(null)
        private set

    fun clearMensagem() {
        mensagem = null
    }

    fun sair(onSucesso: () -> Unit = {}) {
        logout()
        onSucesso()
    }

    // ---------- Dados que as telas observam ----------
    var allTransacoes = mutableStateListOf<Transacao>()
        private set

    var allCategorias = mutableStateListOf<Categoria>()
        private set

    // Transação escolhida para editar (null = criando uma nova)
    var transacaoEmEdicao = mutableStateOf<Transacao?>(null)
        private set

    // Cotações vindas da API (Retrofit)
    var cotacoes by mutableStateOf<CotacoesResponse?>(null)
        private set
    var carregandoCotacoes by mutableStateOf(false)
        private set
    var erroCotacoes by mutableStateOf<String?>(null)
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

    // =====================================================
    // LOGIN / CADASTRO / LOGOUT
    // =====================================================

    // Retorna null se deu certo, ou a mensagem de erro
    fun login(email: String, senha: String): String? {
        if (email.isBlank() || senha.isBlank()) return "Preencha e-mail e senha"

        val usuario = usuarioDAO.autenticar(email.trim(), senha)
            ?: return "E-mail ou senha inválidos"

        iniciarSessao(usuario)
        return null
    }

    // Retorna null se deu certo, ou a mensagem de erro
    fun cadastrar(nome: String, email: String, senha: String, confirmarSenha: String): String? {
        if (nome.isBlank() || email.isBlank() || senha.isBlank()) return "Preencha todos os campos"
        if (!email.contains("@")) return "E-mail inválido"
        if (senha.length < 4) return "A senha deve ter pelo menos 4 caracteres"
        if (senha != confirmarSenha) return "As senhas não conferem"
        if (usuarioDAO.buscarPorEmail(email.trim()) != null) return "E-mail já cadastrado"

        val novoUsuario = Usuario(nome = nome.trim(), email = email.trim(), senha = senha)
        val id = usuarioDAO.inserir(novoUsuario)
        if (id == -1L) return "Erro ao cadastrar usuário"

        criarCategoriasPadrao(id)
        iniciarSessao(novoUsuario.copy(id = id))
        return null
    }

    fun cadastrar(nome: String, email: String, senha: String, onSuccess: () -> Unit) {
        val erro = cadastrar(nome, email, senha, senha)
        if (erro != null) {
            mensagem = erro
        } else {
            mensagem = null
            onSuccess()
        }
    }

    fun logout() {
        prefs.limparSessao()
        allTransacoes.clear()
        allCategorias.clear()
        nomeUsuario.value = ""
        isUserLoggedIn.value = false
    }

    private fun iniciarSessao(usuario: Usuario) {
        prefs.salvarSessao(usuario.id, usuario.nome)
        nomeUsuario.value = usuario.nome
        isUserLoggedIn.value = true
        carregarDados()
    }

    // Todo usuário novo já começa com algumas categorias
    private fun criarCategoriasPadrao(usuarioId: Long) {
        val padroes = listOf(
            Triple("Alimentação", "🍔", "#FF5722"),
            Triple("Transporte", "🚗", "#2196F3"),
            Triple("Lazer", "🎮", "#4CAF50"),
            Triple("Moradia", "🏠", "#9C27B0")
        )
        for ((nome, icone, cor) in padroes) {
            categoriaDAO.inserir(
                Categoria(
                    nome = nome,
                    iconeNome = icone,
                    corHex = cor,
                    limiteMensal = 500.0,
                    usuarioId = usuarioId
                )
            )
        }
    }

    // =====================================================
    // LEITURA DO BANCO (chamada pelo LaunchedEffect das telas)
    // =====================================================

    fun carregarDados() {
        carregarCategorias()
        carregarTransacoes()
        carregarCotacoes()
    }

    fun carregarCotacoes() {
        viewModelScope.launch {
            carregandoCotacoes = true
            erroCotacoes = null
            try {
                cotacoes = RetrofitClient.api.buscarCotacoes()
            } catch (_: Exception) {
                erroCotacoes = "Não foi possível atualizar cotações"
            } finally {
                carregandoCotacoes = false
            }
        }
    }

    fun carregarCategorias() {
        val usuarioId = prefs.getUsuarioId()
        if (usuarioId == -1L) return
        allCategorias.clear()
        allCategorias.addAll(categoriaDAO.listarPorUsuario(usuarioId))
    }

    fun carregarTransacoes() {
        val usuarioId = prefs.getUsuarioId()
        if (usuarioId == -1L) return
        allTransacoes.clear()
        allTransacoes.addAll(transacaoDAO.listarPorUsuario(usuarioId))
        recalcularTotais()
    }

    // =====================================================
    // CRUD 1 - TRANSAÇÃO
    // =====================================================

    fun selecionarTransacaoParaEditar(transacao: Transacao?) {
        transacaoEmEdicao.value = transacao
    }

    fun addTransacao(
        descricao: String,
        valor: Double,
        tipo: String,
        categoriaId: Long,
        efetivado: Boolean,
        localizacao: String = ""
    ) {
        val novaTransacao = Transacao(
            descricao = descricao,
            valor = valor,
            tipo = tipo,
            dataInMillis = System.currentTimeMillis(),
            categoriaId = categoriaId,
            efetivado = efetivado,
            usuarioId = prefs.getUsuarioId(),
            localizacao = localizacao
        )
        transacaoDAO.inserir(novaTransacao)
        carregarTransacoes()
    }

    fun updateTransacao(transacao: Transacao) {
        transacaoDAO.atualizar(transacao)
        carregarTransacoes()
    }

    fun deleteTransacao(transacao: Transacao) {
        transacaoDAO.deletar(transacao.id)
        carregarTransacoes()
    }

    // =====================================================
    // CRUD 2 - CATEGORIA
    // =====================================================

    fun addCategoria(nome: String, icone: String, limite: Double) {
        val cores = listOf("#FF5722", "#2196F3", "#4CAF50", "#9C27B0", "#FFC107", "#00BCD4")
        val novaCategoria = Categoria(
            nome = nome,
            iconeNome = icone.ifBlank { "📁" },
            corHex = cores[allCategorias.size % cores.size],
            limiteMensal = limite,
            usuarioId = prefs.getUsuarioId()
        )
        categoriaDAO.inserir(novaCategoria)
        carregarCategorias()
    }

    fun updateCategoria(categoria: Categoria) {
        categoriaDAO.atualizar(categoria)
        carregarCategorias()
    }

    // Ao apagar uma categoria, as transações dela também são apagadas
    fun deleteCategoria(categoria: Categoria) {
        transacaoDAO.deletarPorCategoria(categoria.id)
        categoriaDAO.deletar(categoria.id)
        carregarDados()
    }

    // =====================================================
    // CÁLCULOS
    // =====================================================

    private fun recalcularTotais() {
        var recEfetivadas = 0.0
        var recPrevistas = 0.0
        var despEfetivadas = 0.0
        var despPrevistas = 0.0

        for ((_, _, valor, tipo, _, _, efetivado) in allTransacoes) {
            if (tipo == "RECEITA") {
                if (efetivado) recEfetivadas += valor else recPrevistas += valor
            } else if (tipo == "DESPESA") {
                if (efetivado) despEfetivadas += valor else despPrevistas += valor
            }
        }

        totalReceitasEfetivadas.doubleValue = recEfetivadas
        totalReceitasPrevistas.doubleValue = recPrevistas
        totalDespesasEfetivadas.doubleValue = despEfetivadas
        totalDespesasPrevistas.doubleValue = despPrevistas
    }

    fun getGastoDaCategoria(categoriaId: Long): Double {
        var gastoTotal = 0.0
        for ((_, _, valor, tipo, _, catId) in allTransacoes) {
            if (catId == categoriaId && tipo == "DESPESA") {
                gastoTotal += valor
            }
        }
        return gastoTotal
    }

    fun getRecentTransacoes(): List<Transacao> {
        return allTransacoes.take(5)
    }
}

class AppViewModelFactory(
    private val prefs: PreferenciasApp,
    private val usuarioDAO: UsuarioDAO,
    private val categoriaDAO: CategoriaDAO,
    private val transacaoDAO: TransacaoDAO
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(prefs, usuarioDAO, categoriaDAO, transacaoDAO) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

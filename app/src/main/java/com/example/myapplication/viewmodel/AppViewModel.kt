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
import com.example.myapplication.database.MetaDAO
import com.example.myapplication.database.TransacaoDAO
import com.example.myapplication.database.UsuarioDAO
import com.example.myapplication.model.Categoria
import com.example.myapplication.model.Meta
import com.example.myapplication.model.Transacao
import com.example.myapplication.model.Usuario
import kotlinx.coroutines.launch

class AppViewModel(
    private val usuarioDAO: UsuarioDAO,
    private val categoriaDAO: CategoriaDAO,
    private val transacaoDAO: TransacaoDAO,
    private val metaDAO: MetaDAO
) : ViewModel() {

    // ---------- Sessão em Memória ----------
    private var usuarioIdLogado: Long = -1L

    private fun getUsuarioId(): Long = usuarioIdLogado

    var isUserLoggedIn = mutableStateOf(false)
        private set

    var nomeUsuario = mutableStateOf("")
        private set

    var usuarioAtual = mutableStateOf<Usuario?>(null)
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

    var allMetas = mutableStateListOf<Meta>()
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
        usuarioIdLogado = -1L
        allTransacoes.clear()
        allCategorias.clear()
        allMetas.clear()
        transacaoEmEdicao.value = null
        usuarioAtual.value = null
        nomeUsuario.value = ""
        isUserLoggedIn.value = false
    }

    private fun iniciarSessao(usuario: Usuario) {
        usuarioIdLogado = usuario.id
        usuarioAtual.value = usuario
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
        carregarMetas()
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
        val usuarioId = getUsuarioId()
        if (usuarioId == -1L) return
        allCategorias.clear()
        allCategorias.addAll(categoriaDAO.listarPorUsuario(usuarioId))
    }

    fun carregarMetas() {
        val usuarioId = getUsuarioId()
        if (usuarioId == -1L) return
        allMetas.clear()
        allMetas.addAll(metaDAO.listarPorUsuario(usuarioId))
    }

    fun carregarUsuario() {
        val usuarioId = getUsuarioId()
        if (usuarioId == -1L) return
        usuarioAtual.value = usuarioDAO.buscarPorId(usuarioId)
    }

    fun carregarTransacoes() {
        val usuarioId = getUsuarioId()
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
            usuarioId = getUsuarioId(),
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
            usuarioId = getUsuarioId()
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
    // CRUD 3 - META FINANCEIRA
    // =====================================================

    fun addMeta(titulo: String, valorAlvo: Double, valorAtual: Double) {
        metaDAO.inserir(
            Meta(
                titulo = titulo.trim(),
                valorAlvo = valorAlvo,
                valorAtual = valorAtual,
                usuarioId = getUsuarioId()
            )
        )
        carregarMetas()
    }

    fun updateMeta(meta: Meta) {
        metaDAO.atualizar(meta)
        carregarMetas()
    }

    fun deleteMeta(meta: Meta) {
        metaDAO.deletar(meta.id, getUsuarioId())
        carregarMetas()
    }

    // =====================================================
    // CRUD 4 - USUÁRIO
    // =====================================================

    fun atualizarUsuario(nome: String, email: String, senha: String): String? {
        val usuario = usuarioAtual.value ?: usuarioDAO.buscarPorId(getUsuarioId())
            ?: return "Não foi possível carregar a conta"
        val nomeNormalizado = nome.trim()
        val emailNormalizado = email.trim()
        if (nomeNormalizado.isBlank() || emailNormalizado.isBlank()) {
            return "Preencha nome e e-mail"
        }
        if (!emailNormalizado.contains("@")) return "E-mail inválido"
        if (senha.isNotBlank() && senha.length < 4) {
            return "A senha deve ter pelo menos 4 caracteres"
        }
        val emailExistente = usuarioDAO.buscarPorEmail(emailNormalizado)
        if (emailExistente != null && emailExistente.id != usuario.id) {
            return "E-mail já cadastrado"
        }

        val atualizado = usuario.copy(
            nome = nomeNormalizado,
            email = emailNormalizado,
            senha = senha.ifBlank { usuario.senha }
        )
        if (usuarioDAO.atualizar(atualizado) == 0) return "Não foi possível atualizar a conta"

        usuarioAtual.value = atualizado
        nomeUsuario.value = atualizado.nome
        return null
    }

    fun excluirConta(): Boolean {
        val usuarioId = getUsuarioId()
        if (usuarioId == -1L) return false

        transacaoDAO.deletarPorUsuario(usuarioId)
        categoriaDAO.deletarPorUsuario(usuarioId)
        metaDAO.deletarPorUsuario(usuarioId)
        if (usuarioDAO.deletar(usuarioId) == 0) return false
        logout()
        return true
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
    private val usuarioDAO: UsuarioDAO,
    private val categoriaDAO: CategoriaDAO,
    private val transacaoDAO: TransacaoDAO,
    private val metaDAO: MetaDAO
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(usuarioDAO, categoriaDAO, transacaoDAO, metaDAO) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

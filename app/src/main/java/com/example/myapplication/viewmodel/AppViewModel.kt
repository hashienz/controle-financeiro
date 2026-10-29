package com.example.myapplication.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AulaRepository
import com.example.myapplication.data.Usuario
import com.example.myapplication.model.Categoria
import com.example.myapplication.model.Transacao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AulaRepository(application)

    var inicializando by mutableStateOf(true)
        private set
    var carregando by mutableStateOf(false)
        private set
    var mensagem by mutableStateOf<String?>(null)
        private set
    var usuario by mutableStateOf<Usuario?>(null)
        private set
    var allTransacoes by mutableStateOf<List<Transacao>>(emptyList())
        private set
    var allCategorias by mutableStateOf(categoriasPadrao())
        private set

    init {
        viewModelScope.launch {
            try {
                val sessao = withContext(Dispatchers.IO) { repository.sessaoAtual() }
                if (sessao != null) {
                    carregarDados(sessao.id)
                    usuario = sessao
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mensagem = "Não foi possível carregar a sessão."
            } finally {
                inicializando = false
            }
        }
    }

    fun clearMensagem() {
        mensagem = null
    }

    fun login(email: String, senha: String, onSuccess: () -> Unit) {
        if (carregando || usuario != null) return
        val emailLimpo = email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(emailLimpo).matches() || senha.isEmpty()) {
            mensagem = "Informe um e-mail válido e a senha."
            return
        }
        carregando = true
        mensagem = null
        viewModelScope.launch {
            try {
                val encontrado = withContext(Dispatchers.IO) {
                    repository.entrar(emailLimpo, senha)
                }
                if (encontrado == null) {
                    mensagem = "E-mail ou senha inválidos."
                } else {
                    carregarDados(encontrado.id)
                    usuario = encontrado
                    onSuccess()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mensagem = "Não foi possível entrar. Tente novamente."
            } finally {
                carregando = false
            }
        }
    }

    fun cadastrar(nome: String, email: String, senha: String, onSuccess: () -> Unit) {
        if (carregando || usuario != null) return
        val nomeLimpo = nome.trim()
        val emailLimpo = email.trim()
        mensagem = when {
            nomeLimpo.length !in 3..100 -> "O nome deve ter entre 3 e 100 caracteres."
            !Patterns.EMAIL_ADDRESS.matcher(emailLimpo).matches() -> "Informe um e-mail válido."
            senha.length !in 8..128 -> "A senha deve ter entre 8 e 128 caracteres."
            else -> null
        }
        if (mensagem != null) return
        carregando = true
        viewModelScope.launch {
            try {
                val novoUsuario = withContext(Dispatchers.IO) {
                    repository.cadastrar(nomeLimpo, emailLimpo, senha)
                }
                if (novoUsuario == null) {
                    mensagem = "Este e-mail já está cadastrado."
                } else {
                    carregarDados(novoUsuario.id)
                    usuario = novoUsuario
                    onSuccess()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mensagem = "Não foi possível criar a conta. Tente novamente."
            } finally {
                carregando = false
            }
        }
    }

    fun sair(onSuccess: () -> Unit) {
        val atual = usuario ?: return
        if (carregando) return
        carregando = true
        mensagem = null
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { repository.sair(atual.id) }
                usuario = null
                allTransacoes = emptyList()
                allCategorias = categoriasPadrao()
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mensagem = "Não foi possível sair. Tente novamente."
            } finally {
                carregando = false
            }
        }
    }

    fun addTransacao(
        descricao: String,
        valor: Double,
        tipo: String,
        categoriaId: Long,
        efetivado: Boolean,
        onSuccess: () -> Unit
    ) {
        val atual = usuario ?: return
        if (carregando) return
        if (!valor.isFinite() || valor <= 0.0 ||
            (tipo != "RECEITA" && tipo != "DESPESA") ||
            allCategorias.none { it.id == categoriaId }
        ) {
            mensagem = "Confira o valor, tipo e categoria do lançamento."
            return
        }
        carregando = true
        mensagem = null
        viewModelScope.launch {
            try {
                val transacoes = withContext(Dispatchers.IO) {
                    repository.inserirTransacao(
                        atual.id, descricao, valor, tipo, categoriaId, efetivado
                    )
                    repository.listarTransacoes(atual.id)
                }
                allTransacoes = transacoes
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mensagem = "Não foi possível salvar o lançamento."
            } finally {
                carregando = false
            }
        }
    }

    fun updateTransacao(
        id: Long,
        descricao: String,
        valor: Double,
        tipo: String,
        categoriaId: Long,
        efetivado: Boolean,
        onSuccess: () -> Unit
    ) {
        val atual = usuario ?: return
        if (carregando) return
        if (!valor.isFinite() || valor <= 0.0 ||
            (tipo != "RECEITA" && tipo != "DESPESA") ||
            allCategorias.none { it.id == categoriaId }
        ) {
            mensagem = "Confira o valor, tipo e categoria do lançamento."
            return
        }
        carregando = true
        mensagem = null
        viewModelScope.launch {
            try {
                val transacoes = withContext(Dispatchers.IO) {
                    repository.atualizarTransacao(
                        id, atual.id, descricao, valor, tipo, categoriaId, efetivado
                    )
                    repository.listarTransacoes(atual.id)
                }
                allTransacoes = transacoes
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mensagem = "Não foi possível atualizar o lançamento."
            } finally {
                carregando = false
            }
        }
    }

    fun deleteTransacao(id: Long, onSuccess: () -> Unit = {}) {
        val atual = usuario ?: return
        if (carregando) return
        carregando = true
        mensagem = null
        viewModelScope.launch {
            try {
                val transacoes = withContext(Dispatchers.IO) {
                    repository.deletarTransacao(id, atual.id)
                    repository.listarTransacoes(atual.id)
                }
                allTransacoes = transacoes
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mensagem = "Não foi possível remover o lançamento."
            } finally {
                carregando = false
            }
        }
    }

    fun updateCategoriaLimite(categoria: Categoria, novoLimite: Double) {
        val atual = usuario ?: return
        if (carregando) return
        if (!novoLimite.isFinite() || novoLimite < 0.0 ||
            allCategorias.none { it.id == categoria.id }
        ) {
            mensagem = "Informe um limite válido."
            return
        }
        carregando = true
        mensagem = null
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.salvarLimite(atual.id, categoria.id, novoLimite)
                }
                allCategorias = allCategorias.map {
                    if (it.id == categoria.id) it.copy(limiteMensal = novoLimite) else it
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mensagem = "Não foi possível salvar o limite."
            } finally {
                carregando = false
            }
        }
    }

    fun deleteCategoriaLimite(categoria: Categoria) {
        val atual = usuario ?: return
        if (carregando) return
        carregando = true
        mensagem = null
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.deletarLimite(atual.id, categoria.id)
                }
                allCategorias = allCategorias.map {
                    if (it.id == categoria.id) it.copy(limiteMensal = 500.0) else it
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mensagem = "Não foi possível remover o limite."
            } finally {
                carregando = false
            }
        }
    }

    val totalReceitasEfetivadas: Double get() = totalPara("RECEITA", true)
    val totalReceitasPrevistas: Double get() = totalPara("RECEITA", false)
    val totalDespesasEfetivadas: Double get() = totalPara("DESPESA", true)
    val totalDespesasPrevistas: Double get() = totalPara("DESPESA", false)

    private fun totalPara(tipo: String, efetivado: Boolean): Double {
        var total = 0.0
        for (transacao in allTransacoes) {
            if (transacao.tipo == tipo && transacao.efetivado == efetivado) {
                total += transacao.valor
            }
        }
        return total
    }

    fun getGastoDaCategoria(categoriaId: Long): Double {
        var total = 0.0
        val formatoMes = SimpleDateFormat("yyyyMM", Locale.ROOT)
        val mesAtual = formatoMes.format(Date())
        for (transacao in allTransacoes) {
            if (transacao.categoriaId == categoriaId &&
                transacao.tipo == "DESPESA" &&
                formatoMes.format(Date(transacao.dataInMillis)) == mesAtual
            ) {
                total += transacao.valor
            }
        }
        return total
    }

    fun getRecentTransacoes(): List<Transacao> = allTransacoes.take(5)

    private suspend fun carregarDados(usuarioId: Long) {
        val transacoes = withContext(Dispatchers.IO) {
            repository.listarTransacoes(usuarioId)
        }
        val categorias = withContext(Dispatchers.IO) {
            categoriasPadrao().map { categoria ->
                categoria.copy(
                    limiteMensal = repository.limiteAtual(usuarioId, categoria.id)
                        ?: categoria.limiteMensal
                )
            }
        }
        allTransacoes = transacoes
        allCategorias = categorias
    }

    private fun categoriasPadrao(): List<Categoria> = listOf(
        Categoria(1, "Alimentação", "🍔", "#FF5722", 500.0),
        Categoria(2, "Transporte", "🚗", "#2196F3", 500.0),
        Categoria(3, "Lazer", "🎮", "#4CAF50", 500.0),
        Categoria(4, "Moradia", "🏠", "#9C27B0", 500.0)
    )
}

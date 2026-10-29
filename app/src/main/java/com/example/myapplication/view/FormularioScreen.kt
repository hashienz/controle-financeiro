package com.example.myapplication.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myapplication.viewmodel.AppViewModel

@Composable
fun FormularioScreen(
    viewModel: AppViewModel,
    navController: NavHostController,
    transacaoId: Long? = null
) {
    var descricao by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("DESPESA") }
    var categoriaIndice by remember { mutableStateOf(-1) }
    var efetivado by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }
    val categorias = viewModel.allCategorias
    val editando = transacaoId != null

    LaunchedEffect(transacaoId) {
        viewModel.clearMensagem()
        if (transacaoId != null) {
            val t = viewModel.allTransacoes.find { it.id == transacaoId }
            if (t != null) {
                descricao = t.descricao
                valor = t.valor.toString()
                tipo = t.tipo
                efetivado = t.efetivado
                val idx = categorias.indexOfFirst { it.id == t.categoriaId }
                categoriaIndice = if (idx >= 0) idx else 0
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = if (editando) "EDITAR LANÇAMENTO" else "NOVO LANÇAMENTO",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = valor,
            onValueChange = { entrada ->
                if (entrada.all { it.isDigit() || it == '.' || it == ',' }) {
                    valor = entrada
                    erro = null
                }
            },
            label = { Text("Valor (R$)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = descricao,
            onValueChange = {
                descricao = it
                erro = null
            },
            label = { Text("Descrição") },
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(onClick = {
            tipo = if (tipo == "DESPESA") "RECEITA" else "DESPESA"
        }) {
            Text("Tipo: " + (if (tipo == "DESPESA") "Despesa (alterar)" else "Receita (alterar)"))
        }
        TextButton(onClick = { efetivado = !efetivado }) {
            Text("Situação: " + (if (efetivado) "Efetivado (alterar)" else "Pendente (alterar)"))
        }
        TextButton(
            onClick = {
                if (categorias.isNotEmpty()) categoriaIndice = (categoriaIndice + 1) % categorias.size
            },
            enabled = categorias.isNotEmpty()
        ) {
            val nome = if (categoriaIndice in categorias.indices) {
                categorias[categoriaIndice].nome
            } else {
                "Selecionar"
            }
            Text("Categoria: " + nome + " (alterar)")
        }
        if (categorias.isEmpty()) Text("Nenhuma categoria disponível.")

        erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        viewModel.mensagem?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (viewModel.carregando) Text("Salvando...")

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                val valorNumerico = valor.replace(',', '.').toDoubleOrNull()
                if (valorNumerico == null || valorNumerico <= 0.0) {
                    erro = "Informe um valor maior que zero."
                } else if (categoriaIndice !in categorias.indices) {
                    erro = "Selecione uma categoria."
                } else {
                    erro = null
                    viewModel.clearMensagem()
                    if (transacaoId != null) {
                        viewModel.updateTransacao(
                            id = transacaoId,
                            descricao = descricao,
                            valor = valorNumerico,
                            tipo = tipo,
                            categoriaId = categorias[categoriaIndice].id,
                            efetivado = efetivado,
                            onSuccess = { navController.popBackStack() }
                        )
                    } else {
                        viewModel.addTransacao(
                            descricao = descricao,
                            valor = valorNumerico,
                            tipo = tipo,
                            categoriaId = categorias[categoriaIndice].id,
                            efetivado = efetivado,
                            onSuccess = { navController.popBackStack() }
                        )
                    }
                }
            },
            enabled = !viewModel.carregando
        ) {
            Text(if (editando) "Salvar alterações" else "Salvar lançamento")
        }
        TextButton(
            onClick = { navController.popBackStack() },
            enabled = !viewModel.carregando
        ) { Text("Voltar") }
    }
}

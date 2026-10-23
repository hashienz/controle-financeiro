package com.example.myapplication.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExtratoScreen(viewModel: AppViewModel) {
    var pesquisa by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf("TODOS") }
    var indice by remember { mutableStateOf(0) }

    val transacoes = viewModel.allTransacoes.filter {
        it.descricao.contains(pesquisa, ignoreCase = true) &&
            (filtro == "TODOS" || it.tipo == filtro)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("EXTRATO", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = pesquisa,
            onValueChange = {
                pesquisa = it
                indice = 0
            },
            label = { Text("Pesquisar transação") },
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = {
            filtro = "TODOS"
            indice = 0
        }) { Text(if (filtro == "TODOS") "Todos ✓" else "Todos") }
        TextButton(onClick = {
            filtro = "RECEITA"
            indice = 0
        }) { Text(if (filtro == "RECEITA") "Receitas ✓" else "Receitas") }
        TextButton(onClick = {
            filtro = "DESPESA"
            indice = 0
        }) { Text(if (filtro == "DESPESA") "Despesas ✓" else "Despesas") }

        Spacer(modifier = Modifier.height(8.dp))
        if (transacoes.isEmpty()) {
            Text("Nenhum lançamento encontrado.")
        } else {
            val indiceAtual = if (indice in transacoes.indices) indice else 0
            val transacao = transacoes[indiceAtual]
            Text("Lançamento " + (indiceAtual + 1) + " de " + transacoes.size)
            Text(formatDate(transacao.dataInMillis))
            Text(transacao.descricao)
            Text(if (transacao.tipo == "RECEITA") "Receita" else "Despesa")
            Text(if (transacao.efetivado) "Efetivado" else "Pendente")
            Text(formatCurrency(transacao.valor))
            if (indiceAtual > 0) {
                TextButton(onClick = { indice = indiceAtual - 1 }) { Text("Anterior") }
            }
            if (indiceAtual < transacoes.lastIndex) {
                TextButton(onClick = { indice = indiceAtual + 1 }) { Text("Próximo") }
            }
        }
    }
}

fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    return formatter.format(Date(millis))
}

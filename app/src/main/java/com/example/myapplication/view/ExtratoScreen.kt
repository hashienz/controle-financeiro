package com.example.myapplication.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myapplication.navigation.Formulario
import com.example.myapplication.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExtratoScreen(viewModel: AppViewModel, navController: NavHostController) {
    var pesquisa by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf("TODOS") }

    LaunchedEffect(Unit) {
        viewModel.clearMensagem()
    }

    val transacoes = viewModel.allTransacoes.filter {
        it.descricao.contains(pesquisa, ignoreCase = true) &&
            (filtro == "TODOS" || it.tipo == filtro)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("EXTRATO", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = pesquisa,
            onValueChange = { pesquisa = it },
            label = { Text("Pesquisar transação") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { filtro = "TODOS" }) {
                Text(if (filtro == "TODOS") "Todos ✓" else "Todos")
            }
            TextButton(onClick = { filtro = "RECEITA" }) {
                Text(if (filtro == "RECEITA") "Receitas ✓" else "Receitas")
            }
            TextButton(onClick = { filtro = "DESPESA" }) {
                Text(if (filtro == "DESPESA") "Despesas ✓" else "Despesas")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        viewModel.mensagem?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (transacoes.isEmpty()) {
            Text("Nenhum lançamento encontrado.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transacoes, key = { it.id }) { transacao ->
                    val categoriaNome = viewModel.allCategorias
                        .find { it.id == transacao.categoriaId }?.nome ?: "Sem Categoria"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = transacao.descricao,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = formatCurrency(transacao.valor),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (transacao.tipo == "RECEITA")
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${formatDate(transacao.dataInMillis)} • $categoriaNome",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = if (transacao.efetivado) "Efetivado" else "Pendente",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    navController.navigate(Formulario(transacaoId = transacao.id))
                                }) {
                                    Text("Editar")
                                }
                                TextButton(onClick = {
                                    viewModel.deleteTransacao(transacao.id)
                                }) {
                                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    return formatter.format(Date(millis))
}

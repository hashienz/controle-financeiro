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
import androidx.compose.material3.Button
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
import com.example.myapplication.viewmodel.AppViewModel

@Composable
fun MetasScreen(viewModel: AppViewModel) {
    val categorias = viewModel.allCategorias
    var categoriaEmEdicaoId by remember { mutableStateOf<Long?>(null) }
    var novoLimite by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.clearMensagem()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("ORÇAMENTOS E METAS", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        viewModel.mensagem?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (categorias.isEmpty()) {
            Text("Nenhuma categoria disponível.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categorias, key = { it.id }) { categoria ->
                    val gasto = viewModel.getGastoDaCategoria(categoria.id)
                    val proporcao = if (categoria.limiteMensal > 0) {
                        gasto / categoria.limiteMensal
                    } else if (gasto > 0) {
                        1.0
                    } else {
                        0.0
                    }
                    val percentual = (proporcao * 100).toInt()
                    val editandoEsta = categoriaEmEdicaoId == categoria.id

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
                                    text = "${categoria.iconeNome} ${categoria.nome}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Uso: $percentual%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (proporcao >= 0.9) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Gasto Atual: ${formatCurrency(gasto)}")
                            Text("Meta Mensal: ${formatCurrency(categoria.limiteMensal)}")

                            if (proporcao >= 0.9) {
                                Text(
                                    "Atenção: meta quase atingida ou ultrapassada.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.colorScheme.error.let { MaterialTheme.typography.bodySmall }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (editandoEsta) {
                                OutlinedTextField(
                                    value = novoLimite,
                                    onValueChange = {
                                        novoLimite = it
                                        erro = null
                                    },
                                    label = { Text("Novo limite mensal (R$)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = {
                                        categoriaEmEdicaoId = null
                                        erro = null
                                    }) {
                                        Text("Cancelar")
                                    }
                                    Button(onClick = {
                                        val limite = novoLimite.replace(',', '.').toDoubleOrNull()
                                        if (limite == null || !limite.isFinite() || limite < 0.0) {
                                            erro = "Informe um limite válido."
                                        } else {
                                            viewModel.updateCategoriaLimite(categoria, limite)
                                            categoriaEmEdicaoId = null
                                            erro = null
                                        }
                                    }) {
                                        Text("Salvar")
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = {
                                        categoriaEmEdicaoId = categoria.id
                                        novoLimite = categoria.limiteMensal.toString()
                                        erro = null
                                    }) {
                                        Text("Ajustar Meta")
                                    }
                                    TextButton(onClick = {
                                        viewModel.deleteCategoriaLimite(categoria)
                                    }) {
                                        Text("Remover Meta", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

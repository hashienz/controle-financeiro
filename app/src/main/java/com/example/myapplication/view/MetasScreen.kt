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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.AppViewModel

@Composable
fun MetasScreen(viewModel: AppViewModel) {
    var indice by remember { mutableStateOf(0) }
    var editando by remember { mutableStateOf(false) }
    var novoLimite by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }
    val categorias = viewModel.allCategorias

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("ORÇAMENTOS E METAS", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (categorias.isEmpty()) {
            Text("Nenhuma categoria disponível.")
        } else {
            val indiceAtual = if (indice in categorias.indices) indice else 0
            val categoria = categorias[indiceAtual]
            val gasto = viewModel.getGastoDaCategoria(categoria.id)
            val proporcao = if (categoria.limiteMensal > 0) {
                gasto / categoria.limiteMensal
            } else if (gasto > 0) {
                1.0
            } else {
                0.0
            }
            val percentual = (proporcao * 100).toInt()

            Text("Categoria " + (indiceAtual + 1) + " de " + categorias.size)
            Text(categoria.iconeNome + " " + categoria.nome)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Gasto: " + formatCurrency(gasto))
            Text("Meta: " + formatCurrency(categoria.limiteMensal))
            if (categoria.limiteMensal == 0.0 && gasto > 0) {
                Text("Uso da meta: limite ultrapassado")
            } else {
                Text("Uso da meta: " + percentual + "%")
            }
            if (proporcao >= 0.9) {
                Text("Atenção: meta quase atingida ou ultrapassada.")
            } else if (proporcao >= 0.7) {
                Text("Atenção: gasto próximo da meta.")
            }

            Spacer(modifier = Modifier.height(12.dp))
            if (editando) {
                OutlinedTextField(
                    value = novoLimite,
                    onValueChange = {
                        novoLimite = it
                        erro = null
                    },
                    label = { Text("Limite mensal (R$)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = {
                    val limite = novoLimite.replace(',', '.').toDoubleOrNull()
                    if (limite == null || !limite.isFinite() || limite < 0.0) {
                        erro = "Informe um limite válido."
                    } else {
                        viewModel.updateCategoriaLimite(categoria, limite)
                        editando = false
                    }
                }) { Text("Salvar meta") }
                TextButton(onClick = { editando = false }) { Text("Cancelar") }
                erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            } else {
                TextButton(onClick = {
                    novoLimite = categoria.limiteMensal.toString()
                    erro = null
                    editando = true
                }) { Text("Ajustar meta") }
            }

            viewModel.mensagem?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Spacer(modifier = Modifier.height(12.dp))
            if (indiceAtual > 0) {
                TextButton(onClick = {
                    indice = indiceAtual - 1
                    editando = false
                }) { Text("Categoria anterior") }
            }
            if (indiceAtual < categorias.lastIndex) {
                TextButton(onClick = {
                    indice = indiceAtual + 1
                    editando = false
                }) { Text("Próxima categoria") }
            }
        }
    }
}

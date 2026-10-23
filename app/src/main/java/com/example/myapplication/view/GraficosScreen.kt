package com.example.myapplication.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Categoria
import com.example.myapplication.viewmodel.AppViewModel

@Composable
fun GraficosScreen(viewModel: AppViewModel) {
    var indice by remember { mutableStateOf(0) }
    val categoriasComGasto = mutableListOf<Pair<Categoria, Double>>()
    var totalGasto = 0.0

    for (categoria in viewModel.allCategorias) {
        val gasto = viewModel.getGastoDaCategoria(categoria.id)
        if (gasto > 0) {
            categoriasComGasto.add(Pair(categoria, gasto))
            totalGasto += gasto
        }
    }
    categoriasComGasto.sortByDescending { it.second }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("DISTRIBUIÇÃO DE DESPESAS", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (categoriasComGasto.isEmpty()) {
            Text("Sem despesas para exibir.")
        } else {
            val indiceAtual = if (indice in categoriasComGasto.indices) indice else 0
            val item = categoriasComGasto[indiceAtual]
            val percentual = (item.second / totalGasto * 100).toInt()

            Text("Total de despesas: " + formatCurrency(totalGasto))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Categoria " + (indiceAtual + 1) + " de " + categoriasComGasto.size)
            Text(item.first.nome)
            Text("Gasto: " + formatCurrency(item.second))
            Text("Participação: " + percentual + "%")

            Spacer(modifier = Modifier.height(12.dp))
            if (indiceAtual > 0) {
                TextButton(onClick = { indice = indiceAtual - 1 }) { Text("Anterior") }
            }
            if (indiceAtual < categoriasComGasto.lastIndex) {
                TextButton(onClick = { indice = indiceAtual + 1 }) { Text("Próximo") }
            }
        }
    }
}

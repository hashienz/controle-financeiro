package com.example.myapplication.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.navigation.NavHostController
import com.example.myapplication.navigation.Formulario
import com.example.myapplication.viewmodel.AppViewModel

@Composable
fun HomeScreen(viewModel: AppViewModel, navController: NavHostController) {
    var mostrarValores by remember { mutableStateOf(true) }
    var indiceRecente by remember { mutableStateOf(0) }

    val saldo = viewModel.totalReceitasEfetivadas - viewModel.totalDespesasEfetivadas
    val previsto = viewModel.totalReceitasPrevistas - viewModel.totalDespesasPrevistas
    val recentes = viewModel.getRecentTransacoes()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("LUME", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Text("SALDO DISPONÍVEL")
        Text(
            if (mostrarValores) formatCurrency(saldo) else "••••••",
            style = MaterialTheme.typography.headlineMedium
        )
        Text("Previsto: " + (if (mostrarValores) formatCurrency(previsto) else "••••"))
        TextButton(onClick = { mostrarValores = !mostrarValores }) {
            Text(if (mostrarValores) "Ocultar valores" else "Mostrar valores")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Entradas: " + (if (mostrarValores) formatCurrency(viewModel.totalReceitasEfetivadas) else "•••"))
        Text("Saídas: " + (if (mostrarValores) formatCurrency(viewModel.totalDespesasEfetivadas) else "•••"))

        Spacer(modifier = Modifier.height(20.dp))
        Text("Atividade recente")
        if (recentes.isEmpty()) {
            Text("Nenhuma transação cadastrada.")
        } else {
            val indice = if (indiceRecente in recentes.indices) indiceRecente else 0
            val transacao = recentes[indice]
            Text("Lançamento " + (indice + 1) + " de " + recentes.size)
            Text(transacao.descricao)
            Text(if (transacao.tipo == "RECEITA") "Receita" else "Despesa")
            Text(if (transacao.efetivado) "Efetivado" else "Pendente")
            Text(if (mostrarValores) formatCurrency(transacao.valor) else "•••")
            if (indice > 0) {
                TextButton(onClick = { indiceRecente = indice - 1 }) { Text("Anterior") }
            }
            if (indice < recentes.lastIndex) {
                TextButton(onClick = { indiceRecente = indice + 1 }) { Text("Próximo") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate(Formulario) }) {
            Text("Nova transação")
        }
    }
}

fun formatCurrency(valor: Double): String {
    val negativo = valor < 0
    val absoluto = if (negativo) -valor else valor
    val centavos = (absoluto * 100 + 0.5).toLong()
    val reais = centavos / 100
    val centavosRestantes = centavos % 100
    val centavosTexto = if (centavosRestantes < 10) "0" + centavosRestantes else centavosRestantes.toString()
    val digitos = reais.toString()
    var posicao = digitos.length
    var reaisTexto = ""
    while (posicao > 3) {
        reaisTexto = "." + digitos.substring(posicao - 3, posicao) + reaisTexto
        posicao -= 3
    }
    reaisTexto = digitos.substring(0, posicao) + reaisTexto
    return (if (negativo) "-R$ " else "R$ ") + reaisTexto + "," + centavosTexto
}

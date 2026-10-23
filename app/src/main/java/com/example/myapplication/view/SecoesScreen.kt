package com.example.myapplication.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SecoesScreen(
    irFormulario: () -> Unit,
    irExtrato: () -> Unit,
    irMetas: () -> Unit,
    irGraficos: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Seções", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Button(onClick = irFormulario, modifier = Modifier.fillMaxWidth()) {
            Text("Novo lançamento")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = irExtrato, modifier = Modifier.fillMaxWidth()) {
            Text("Extrato")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = irMetas, modifier = Modifier.fillMaxWidth()) {
            Text("Metas")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = irGraficos, modifier = Modifier.fillMaxWidth()) {
            Text("Gráficos")
        }
    }
}

package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.CategoriaEntity
import com.example.myapplication.viewmodel.AppViewModel
import com.example.myapplication.ui.theme.*

@Composable
fun GraficosScreen(viewModel: AppViewModel) {
    // Preparar dados (Didático)
    val categoriasComGasto = mutableListOf<Pair<CategoriaEntity, Double>>()
    var totalGasto = 0.0

    for (categoria in viewModel.allCategorias) {
        val gasto = viewModel.getGastoDaCategoria(categoria.id)
        if (gasto > 0) {
            categoriasComGasto.add(Pair(categoria, gasto))
            totalGasto += gasto
        }
    }

    // Ordenar do maior gasto para o menor
    categoriasComGasto.sortByDescending { it.second }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LumeBg)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "DISTRIBUIÇÃO DE DESPESAS",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = LumeTextSecondary,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (totalGasto > 0) {
            // Um card de resumo simples em vez de gráfico complexo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LumeSurface)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("TOTAL GASTO ESTE MÊS", style = MaterialTheme.typography.labelSmall, color = LumeTextSecondary)
                Text(
                    formatCurrency(totalGasto),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = LumeError,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(categoriasComGasto) { (categoria, gasto) ->
                    LumeReportLegendItem(categoria.nome, gasto, totalGasto, categoria.corHex)
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sem despesas para exibir.", color = LumeTextSecondary)
            }
        }
    }
}

@Composable
fun LumeReportLegendItem(label: String, valor: Double, total: Double, colorHex: String) {
    val percent = (valor / total * 100).toInt()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LumeSurface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { LumeAccent }
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = LumeTextPrimary, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(formatCurrency(valor), fontWeight = FontWeight.Bold, color = LumeTextPrimary)
            Text("$percent%", style = MaterialTheme.typography.labelSmall, color = LumeTextSecondary)
        }
    }
}

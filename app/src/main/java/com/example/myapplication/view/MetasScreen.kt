package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
fun MetasScreen(viewModel: AppViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedCategoria by remember { mutableStateOf<CategoriaEntity?>(null) }
    var novoLimite by remember { mutableStateOf("") }

    if (showDialog && selectedCategoria != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = LumeSurface,
            title = { 
                Text(
                    "AJUSTAR META", 
                    color = LumeTextPrimary, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 18.sp 
                ) 
            },
            text = {
                Column {
                    Text(
                        selectedCategoria?.nome ?: "", 
                        color = LumeAccent, 
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = novoLimite,
                        onValueChange = { novoLimite = it },
                        label = { Text("Limite Mensal (R$)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = LumeBorder,
                            focusedBorderColor = LumeAccent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limite = novoLimite.toDoubleOrNull() ?: 0.0
                        viewModel.updateCategoriaLimite(selectedCategoria!!, limite)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LumeAccent)
                ) {
                    Text("Salvar", color = LumeBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar", color = LumeTextSecondary)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LumeBg)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "ORÇAMENTOS E METAS",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = LumeTextSecondary,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(viewModel.allCategorias) { categoria ->
                // Cálculo de Gasto Direto (Didático)
                val gasto = viewModel.getGastoDaCategoria(categoria.id)
                MetasLumeItem(categoria, gasto) {
                    selectedCategoria = categoria
                    novoLimite = categoria.limiteMensal.toString()
                    showDialog = true
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun MetasLumeItem(categoria: CategoriaEntity, gasto: Double, onEdit: () -> Unit) {
    val progress = if (categoria.limiteMensal > 0) gasto / categoria.limiteMensal else 0.0
    
    val color = when {
        progress >= 0.9 -> LumeError
        progress >= 0.7 -> Color(0xFFFBC02D)
        else -> LumeAccent
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LumeSurface)
            .border(1.dp, LumeBorder, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${categoria.iconeNome} ${categoria.nome.uppercase()}", 
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = LumeTextPrimary,
                letterSpacing = 1.sp
            )
            IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = LumeTextSecondary, modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LinearProgressIndicator(
            progress = { progress.toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = color,
            trackColor = LumeBg
        )
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("GASTO", style = MaterialTheme.typography.labelSmall, color = LumeTextSecondary)
                Text(formatCurrency(gasto), fontWeight = FontWeight.Bold, color = LumeTextPrimary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("META", style = MaterialTheme.typography.labelSmall, color = LumeTextSecondary)
                Text(formatCurrency(categoria.limiteMensal), fontWeight = FontWeight.Bold, color = LumeTextSecondary)
            }
        }
    }
}

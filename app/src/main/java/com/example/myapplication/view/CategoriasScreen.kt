package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Categoria
import com.example.myapplication.theme.*
import com.example.myapplication.viewmodel.AppViewModel

@Composable
fun CategoriasScreen(viewModel: AppViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    // null = criando categoria nova; preenchido = editando
    var selectedCategoria by remember { mutableStateOf<Categoria?>(null) }
    var categoriaParaExcluir by remember { mutableStateOf<Categoria?>(null) }

    var nome by remember { mutableStateOf("") }
    var icone by remember { mutableStateOf("") }
    var novoLimite by remember { mutableStateOf("") }

    // Busca as categorias no banco ao abrir a tela
    LaunchedEffect(Unit) {
        viewModel.carregarDados()
    }

    // Diálogo de CRIAR / ALTERAR categoria
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = LumeSurface,
            title = {
                Text(
                    if (selectedCategoria == null) "NOVA CATEGORIA" else "EDITAR CATEGORIA",
                    color = LumeTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nome,
                        onValueChange = { nome = it },
                        label = { Text("Nome") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = LumeBorder,
                            focusedBorderColor = LumeAccent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = icone,
                        onValueChange = { icone = it },
                        label = { Text("Ícone (emoji)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = LumeBorder,
                            focusedBorderColor = LumeAccent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = novoLimite,
                        onValueChange = { novoLimite = it },
                        label = { Text("Limite Mensal (R$)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                        val limite = novoLimite.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val categoria = selectedCategoria
                        if (nome.isNotBlank()) {
                            if (categoria == null) {
                                viewModel.addCategoria(nome.trim(), icone.trim(), limite)          // CREATE
                            } else {
                                viewModel.updateCategoria(                                         // UPDATE
                                    categoria.copy(
                                        nome = nome.trim(),
                                        iconeNome = icone.trim().ifBlank { categoria.iconeNome },
                                        limiteMensal = limite
                                    )
                                )
                            }
                            showDialog = false
                        }
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

    // Diálogo de confirmação do DELETE
    if (categoriaParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { categoriaParaExcluir = null },
            containerColor = LumeSurface,
            title = { Text("Excluir categoria?", color = LumeTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "A categoria \"${categoriaParaExcluir!!.nome}\" e os lançamentos dela serão apagados.",
                    color = LumeTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCategoria(categoriaParaExcluir!!)
                        categoriaParaExcluir = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LumeError)
                ) { Text("Excluir", color = LumeTextPrimary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { categoriaParaExcluir = null }) {
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "CATEGORIAS E ORÇAMENTOS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = LumeTextSecondary,
                letterSpacing = 2.sp
            )
            TextButton(onClick = {
                selectedCategoria = null
                nome = ""
                icone = ""
                novoLimite = ""
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = null, tint = LumeAccent)
                Text("Nova", color = LumeAccent, fontWeight = FontWeight.Bold)
            }
        }

        if (viewModel.allCategorias.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhuma categoria cadastrada.", color = LumeTextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(viewModel.allCategorias) { categoria ->
                    val gasto = viewModel.getGastoDaCategoria(categoria.id)
                    BudgetLumeItem(
                        categoria = categoria,
                        gasto = gasto,
                        onEdit = {
                            selectedCategoria = categoria
                            nome = categoria.nome
                            icone = categoria.iconeNome
                            novoLimite = categoria.limiteMensal.toString()
                            showDialog = true
                        },
                        onDelete = { categoriaParaExcluir = categoria }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun BudgetLumeItem(categoria: Categoria, gasto: Double, onEdit: () -> Unit, onDelete: () -> Unit) {
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
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = LumeTextSecondary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = LumeError, modifier = Modifier.size(18.dp))
                }
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

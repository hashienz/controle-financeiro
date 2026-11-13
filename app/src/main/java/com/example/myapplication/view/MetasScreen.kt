package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Meta
import com.example.myapplication.theme.LumeAccent
import com.example.myapplication.theme.LumeBg
import com.example.myapplication.theme.LumeBorder
import com.example.myapplication.theme.LumeError
import com.example.myapplication.theme.LumeSurface
import com.example.myapplication.theme.LumeTextPrimary
import com.example.myapplication.theme.LumeTextSecondary
import com.example.myapplication.viewmodel.AppViewModel

@Composable
fun MetasScreen(viewModel: AppViewModel) {
    var mostrarFormulario by remember { mutableStateOf(false) }
    var metaSelecionada by remember { mutableStateOf<Meta?>(null) }
    var metaParaExcluir by remember { mutableStateOf<Meta?>(null) }
    var titulo by remember { mutableStateOf("") }
    var alvo by remember { mutableStateOf("") }
    var atual by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.carregarMetas()
    }

    if (mostrarFormulario) {
        AlertDialog(
            onDismissRequest = { mostrarFormulario = false },
            containerColor = LumeSurface,
            title = {
                Text(
                    if (metaSelecionada == null) "NOVA META" else "EDITAR META",
                    color = LumeTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it; erro = null },
                        label = { Text("Objetivo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = alvo,
                        onValueChange = { alvo = it; erro = null },
                        label = { Text("Valor desejado (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = atual,
                        onValueChange = { atual = it; erro = null },
                        label = { Text("Valor guardado (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    erro?.let { Text(it, color = LumeError) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valorAlvo = alvo.replace(",", ".").toDoubleOrNull()
                        val valorAtual = atual.ifBlank { "0" }.replace(",", ".").toDoubleOrNull()
                        when {
                            titulo.isBlank() -> erro = "Informe o objetivo"
                            valorAlvo == null || valorAlvo <= 0 -> erro = "Informe um valor desejado maior que zero"
                            valorAtual == null || valorAtual < 0 -> erro = "Informe um valor guardado válido"
                            else -> {
                                val meta = metaSelecionada
                                if (meta == null) {
                                    viewModel.addMeta(titulo, valorAlvo, valorAtual)
                                } else {
                                    viewModel.updateMeta(
                                        meta.copy(
                                            titulo = titulo.trim(),
                                            valorAlvo = valorAlvo,
                                            valorAtual = valorAtual
                                        )
                                    )
                                }
                                mostrarFormulario = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LumeAccent)
                ) {
                    Text("Salvar", color = LumeBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarFormulario = false }) {
                    Text("Cancelar", color = LumeTextSecondary)
                }
            }
        )
    }

    metaParaExcluir?.let { meta ->
        AlertDialog(
            onDismissRequest = { metaParaExcluir = null },
            containerColor = LumeSurface,
            title = { Text("Excluir meta?", color = LumeTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("A meta \"${meta.titulo}\" será removida.", color = LumeTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMeta(meta)
                        metaParaExcluir = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LumeError)
                ) {
                    Text("Excluir", color = LumeTextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { metaParaExcluir = null }) {
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "METAS FINANCEIRAS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = LumeTextSecondary,
                letterSpacing = 2.sp
            )
            TextButton(onClick = {
                metaSelecionada = null
                titulo = ""
                alvo = ""
                atual = ""
                erro = null
                mostrarFormulario = true
            }) {
                Icon(Icons.Default.Add, contentDescription = null, tint = LumeAccent)
                Text("Nova", color = LumeAccent, fontWeight = FontWeight.Bold)
            }
        }

        if (viewModel.allMetas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhuma meta cadastrada.", color = LumeTextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(viewModel.allMetas, key = { it.id }) { meta ->
                    MetaLumeItem(
                        meta = meta,
                        onEdit = {
                            metaSelecionada = meta
                            titulo = meta.titulo
                            alvo = meta.valorAlvo.toString()
                            atual = meta.valorAtual.toString()
                            erro = null
                            mostrarFormulario = true
                        },
                        onDelete = { metaParaExcluir = meta }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun MetaLumeItem(meta: Meta, onEdit: () -> Unit, onDelete: () -> Unit) {
    val progresso = (meta.valorAtual / meta.valorAlvo).toFloat().coerceIn(0f, 1f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LumeSurface)
            .border(1.dp, LumeBorder, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(meta.titulo, color = LumeTextPrimary, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar meta", tint = LumeTextSecondary)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir meta", tint = LumeError)
                }
            }
        }
        LinearProgressIndicator(
            progress = { progresso },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = if (progresso >= 1f) Color(0xFF4CAF50) else LumeAccent,
            trackColor = LumeBg
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatCurrency(meta.valorAtual), color = LumeTextPrimary, fontWeight = FontWeight.Bold)
            Text("de ${formatCurrency(meta.valorAlvo)}", color = LumeTextSecondary)
        }
    }
}

package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.model.CategoriaEntity
import com.example.myapplication.viewmodel.AppViewModel
import com.example.myapplication.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(viewModel: AppViewModel, navController: NavController) {
    var descricao by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("DESPESA") }
    var selectedCategoriaId by remember { mutableStateOf<Long?>(null) }
    var efetivado by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = LumeBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "NOVO LANÇAMENTO", 
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Voltar",
                            tint = LumeTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = LumeBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Valor Simples
            OutlinedTextField(
                value = valor,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) valor = it },
                label = { Text("Valor (R$)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = LumeBorder,
                    focusedBorderColor = LumeAccent
                )
            )

            // Seletor de Tipo com Botões Simples (Didático)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { tipo = "RECEITA" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tipo == "RECEITA") LumeAccent else LumeSurface,
                        contentColor = if (tipo == "RECEITA") LumeBg else LumeTextSecondary
                    )
                ) {
                    Text("Receita")
                }
                Button(
                    onClick = { tipo = "DESPESA" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tipo == "DESPESA") LumeError else LumeSurface,
                        contentColor = if (tipo == "DESPESA") LumeTextPrimary else LumeTextSecondary
                    )
                ) {
                    Text("Despesa")
                }
            }

            // Descrição
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = LumeBorder,
                    focusedBorderColor = LumeAccent,
                    unfocusedContainerColor = LumeSurface,
                    focusedContainerColor = LumeSurface
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            // Switch Efetivado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Lançamento Efetivado (Pago/Recebido)", color = LumeTextSecondary)
                Switch(
                    checked = efetivado,
                    onCheckedChange = { efetivado = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = LumeAccent, checkedTrackColor = LumeSurface)
                )
            }

            // Seleção de Categorias
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "CATEGORIA", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = LumeTextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.allCategorias) { categoria ->
                        CategoryGridItem(
                            categoria = categoria,
                            isSelected = selectedCategoriaId == categoria.id,
                            onClick = { selectedCategoriaId = categoria.id }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val v = valor.toDoubleOrNull() ?: 0.0
                    if (v > 0 && selectedCategoriaId != null) {
                        viewModel.addTransacao(descricao, v, tipo, selectedCategoriaId!!, efetivado)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 8.dp),
                enabled = valor.isNotEmpty() && selectedCategoriaId != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tipo == "RECEITA") LumeAccent else LumeAccent,
                    contentColor = LumeBg
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SALVAR LANÇAMENTO", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
fun CategoryGridItem(categoria: CategoriaEntity, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) LumeSurface else Color.Transparent)
            .border(
                1.dp, 
                if (isSelected) LumeAccent else LumeBorder, 
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(LumeBg),
            contentAlignment = Alignment.Center
        ) {
            Text(categoria.iconeNome, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = categoria.nome,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) LumeTextPrimary else LumeTextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

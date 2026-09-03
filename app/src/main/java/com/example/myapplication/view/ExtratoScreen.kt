package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.viewmodel.AppViewModel
import com.example.myapplication.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExtratoScreen(viewModel: AppViewModel) {
    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("TODOS") }

    // Lógica simples de filtragem (Didático)
    val filteredTransacoes = viewModel.allTransacoes.filter {
        (it.descricao.contains(searchText, ignoreCase = true)) &&
        (selectedFilter == "TODOS" || it.tipo == selectedFilter)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LumeBg)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Pesquisar transação...", color = LumeTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LumeTextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = LumeBorder,
                focusedBorderColor = LumeAccent,
                unfocusedContainerColor = LumeSurface,
                focusedContainerColor = LumeSurface
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        // Filtros usando Row e Botões simples em vez de Chips avançados
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterButton("Todos", selectedFilter == "TODOS") { selectedFilter = "TODOS" }
            FilterButton("Receitas", selectedFilter == "RECEITA") { selectedFilter = "RECEITA" }
            FilterButton("Despesas", selectedFilter == "DESPESA") { selectedFilter = "DESPESA" }
        }

        if (filteredTransacoes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum lançamento encontrado.", color = LumeTextSecondary)
            }
        } else {
            // LazyColumn clássico sem Sticky Headers (mais didático)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTransacoes) { transacao ->
                    Column {
                        Text(
                            text = formatDate(transacao.dataInMillis),
                            style = MaterialTheme.typography.labelSmall,
                            color = LumeTextSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        TransactionLumeItem(transacao, showValues = true)
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun RowScope.FilterButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) LumeAccent else LumeSurface,
            contentColor = if (isSelected) LumeBg else LumeTextSecondary
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(label, fontSize = 12.sp)
    }
}

fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    return formatter.format(Date(millis))
}

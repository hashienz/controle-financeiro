package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.model.TransacaoEntity
import com.example.myapplication.viewmodel.AppViewModel
import com.example.myapplication.navigation.Screen
import com.example.myapplication.ui.theme.*
import java.text.NumberFormat
import java.util.*

@Composable
fun HomeScreen(viewModel: AppViewModel, navController: NavController) {
    // Usando State Simples (Didático)
    var showValues by remember { mutableStateOf(true) }
    
    val saldoEfetivado = viewModel.totalReceitasEfetivadas.doubleValue - viewModel.totalDespesasEfetivadas.doubleValue
    val saldoPrevisto = viewModel.totalReceitasPrevistas.doubleValue - viewModel.totalDespesasPrevistas.doubleValue

    Scaffold(
        containerColor = LumeBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.TransactionForm.route) },
                containerColor = LumeAccent,
                contentColor = LumeBg,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Transação")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SaldoLumeCard(
                    saldoEfetivado, 
                    saldoPrevisto, 
                    showValues, 
                    onToggleVisibility = { showValues = !showValues }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResumoLumeItem(
                        label = "Entradas",
                        valor = viewModel.totalReceitasEfetivadas.doubleValue,
                        modifier = Modifier.weight(1f)
                    )
                    ResumoLumeItem(
                        label = "Saídas",
                        valor = viewModel.totalDespesasEfetivadas.doubleValue,
                        color = LumeError,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "Atividade Recente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LumeTextPrimary
                )
            }

            if (viewModel.recentTransacoes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nenhuma transação cadastrada.", 
                            color = LumeTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(viewModel.recentTransacoes) { transacao ->
                    TransactionLumeItem(transacao, showValues)
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SaldoLumeCard(
    saldo: Double, 
    previsto: Double, 
    showValues: Boolean, 
    onToggleVisibility: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(LumeSurface)
            .border(1.dp, LumeBorder, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "SALDO DISPONÍVEL", 
                style = MaterialTheme.typography.labelMedium,
                color = LumeTextSecondary,
                letterSpacing = 1.sp
            )
            IconButton(onClick = onToggleVisibility, modifier = Modifier.size(24.dp)) {
                Icon(
                    if (showValues) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    tint = LumeTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        Text(
            text = if (showValues) formatCurrency(saldo) else "••••••",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LumeTextPrimary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Text(
            text = "Previsto: ${if (showValues) formatCurrency(previsto) else "••••"}",
            style = MaterialTheme.typography.bodySmall,
            color = LumeTextSecondary
        )
    }
}

@Composable
fun ResumoLumeItem(
    label: String, 
    valor: Double, 
    color: Color = LumeAccent, 
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(LumeSurface)
            .border(1.dp, LumeBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = LumeTextSecondary)
        Text(
            text = formatCurrency(valor),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun TransactionLumeItem(transacao: TransacaoEntity, showValues: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LumeSurface)
                    .border(1.dp, LumeBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transacao.tipo == "RECEITA") Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = if (transacao.tipo == "RECEITA") LumeAccent else LumeError,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    transacao.descricao, 
                    fontWeight = FontWeight.Bold, 
                    color = LumeTextPrimary,
                    fontSize = 15.sp
                )
                Text(
                    if (transacao.efetivado) "Efetivado" else "Pendente",
                    style = MaterialTheme.typography.bodySmall,
                    color = LumeTextSecondary
                )
            }
        }
        Text(
            text = if (showValues) formatCurrency(transacao.valor) else "•••",
            fontWeight = FontWeight.Bold,
            color = if (transacao.tipo == "RECEITA") LumeAccent else LumeTextPrimary,
            fontSize = 15.sp
        )
    }
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return format.format(amount)
}

package com.example.myapplication.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.myapplication.model.Categoria
import com.example.myapplication.theme.*
import com.example.myapplication.viewmodel.AppViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    viewModel: AppViewModel,
    navController: NavController,
    transacaoId: Long? = null
) {
    val context = LocalContext.current
    var descricao by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("DESPESA") }
    var selectedCategoriaId by remember { mutableStateOf<Long?>(null) }
    var efetivado by remember { mutableStateOf(true) }
    var localizacao by remember { mutableStateOf("") }
    var obtendoLocal by remember { mutableStateOf(false) }

    // Se veio uma transação selecionada, a tela está em modo ALTERAR
    val transacaoEmEdicao = transacaoId?.let { id ->
        viewModel.allTransacoes.firstOrNull { it.id == id }
    } ?: viewModel.transacaoEmEdicao.value
    val modoEdicao = transacaoEmEdicao != null

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedida ->
        if (concedida) {
            obtendoLocal = true
            obterLocalAtual(context) { resultado ->
                localizacao = resultado
                obtendoLocal = false
            }
        } else {
            localizacao = "Permissão de localização negada"
        }
    }

    // Ao abrir: carrega as categorias do banco e, se for edição, preenche os campos
    LaunchedEffect(Unit) {
        viewModel.carregarCategorias()
        if (transacaoId != null) viewModel.carregarTransacoes()
    }

    LaunchedEffect(transacaoEmEdicao?.id) {
        if (transacaoEmEdicao != null) {
            descricao = transacaoEmEdicao.descricao
            valor = transacaoEmEdicao.valor.toString()
            tipo = transacaoEmEdicao.tipo
            selectedCategoriaId = transacaoEmEdicao.categoriaId
            efetivado = transacaoEmEdicao.efetivado
            localizacao = transacaoEmEdicao.localizacao
        }
    }

    Scaffold(
        containerColor = LumeBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (modoEdicao) "EDITAR LANÇAMENTO" else "NOVO LANÇAMENTO",
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
                actions = {
                    if (transacaoEmEdicao != null) {
                        IconButton(onClick = {
                            viewModel.deleteTransacao(transacaoEmEdicao)
                            viewModel.selecionarTransacaoParaEditar(null)
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = LumeError)
                        }
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

            // Seletor de Tipo
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

            // Recurso Nativo: Localização do celular
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LumeSurface)
                    .border(1.dp, LumeBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = LumeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (localizacao.isNotBlank()) localizacao else "Nenhum local registrado",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (localizacao.isNotBlank()) LumeTextPrimary else LumeTextSecondary
                    )
                }
                TextButton(
                    onClick = {
                        val temPermissao = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        if (temPermissao) {
                            obtendoLocal = true
                            obterLocalAtual(context) { resultado ->
                                localizacao = resultado
                                obtendoLocal = false
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        }
                    },
                    enabled = !obtendoLocal
                ) {
                    Text(
                        text = if (obtendoLocal) "Buscando..." else "Usar GPS",
                        color = LumeAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            
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
                    modifier = Modifier.height(160.dp),
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
                        if (transacaoEmEdicao != null) {
                            // UPDATE
                            viewModel.updateTransacao(
                                transacaoEmEdicao.copy(
                                    descricao = descricao,
                                    valor = v,
                                    tipo = tipo,
                                    categoriaId = selectedCategoriaId!!,
                                    efetivado = efetivado,
                                    localizacao = localizacao
                                )
                            )
                        } else {
                            // CREATE
                            viewModel.addTransacao(
                                descricao = descricao,
                                valor = v,
                                tipo = tipo,
                                categoriaId = selectedCategoriaId!!,
                                efetivado = efetivado,
                                localizacao = localizacao
                            )
                        }
                        viewModel.selecionarTransacaoParaEditar(null)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 8.dp),
                enabled = valor.isNotEmpty() && selectedCategoriaId != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LumeAccent,
                    contentColor = LumeBg
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (modoEdicao) "SALVAR ALTERAÇÕES" else "SALVAR LANÇAMENTO", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
fun CategoryGridItem(categoria: Categoria, isSelected: Boolean, onClick: () -> Unit) {
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

@SuppressLint("MissingPermission")
private fun obterLocalAtual(context: Context, onResult: (String) -> Unit) {
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) {
                val endereco = try {
                    @Suppress("DEPRECATION")
                    val list = Geocoder(context, Locale("pt", "BR"))
                        .getFromLocation(location.latitude, location.longitude, 1)
                    val addr = list?.firstOrNull()
                    val cidade = addr?.locality ?: addr?.subAdminArea
                    val bairro = addr?.subLocality
                    when {
                        !bairro.isNullOrBlank() && !cidade.isNullOrBlank() -> "$bairro, $cidade"
                        !cidade.isNullOrBlank() -> cidade
                        else -> String.format(Locale.US, "%.4f, %.4f", location.latitude, location.longitude)
                    }
                } catch (_: Exception) {
                    String.format(Locale.US, "%.4f, %.4f", location.latitude, location.longitude)
                }
                onResult(endereco)
            } else {
                onResult("Localização indisponível (ative o GPS)")
            }
        }
        .addOnFailureListener {
            onResult("Erro ao obter localização")
        }
}


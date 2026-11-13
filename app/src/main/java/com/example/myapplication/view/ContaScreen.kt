package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.navigation.Home
import com.example.myapplication.navigation.Login
import com.example.myapplication.theme.LumeAccent
import com.example.myapplication.theme.LumeBg
import com.example.myapplication.theme.LumeError
import com.example.myapplication.theme.LumeSurface
import com.example.myapplication.theme.LumeTextPrimary
import com.example.myapplication.theme.LumeTextSecondary
import com.example.myapplication.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContaScreen(viewModel: AppViewModel, navController: NavController) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }
    var sucesso by remember { mutableStateOf<String?>(null) }
    var confirmarExclusao by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.carregarUsuario()
    }
    val usuario = viewModel.usuarioAtual.value
    LaunchedEffect(usuario?.id) {
        if (usuario != null) {
            nome = usuario.nome
            email = usuario.email
        }
    }

    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = { confirmarExclusao = false },
            containerColor = LumeSurface,
            title = { Text("Excluir conta?", color = LumeTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Sua conta, transações, categorias e metas serão apagadas permanentemente.",
                    color = LumeTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.excluirConta()) {
                            navController.navigate(Login) {
                                popUpTo<Home> { inclusive = true }
                            }
                        } else {
                            erro = "Não foi possível excluir a conta"
                        }
                        confirmarExclusao = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LumeError)
                ) {
                    Text("Excluir conta", color = LumeTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarExclusao = false }) {
                    Text("Cancelar", color = LumeTextSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = LumeBg,
        topBar = {
            TopAppBar(
                title = { Text("MINHA CONTA", color = LumeTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LumeBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LumeBg)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (usuario == null) {
                Text("Não foi possível carregar a conta.", color = LumeTextSecondary)
            } else {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it; erro = null; sucesso = null },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; erro = null; sucesso = null },
                    label = { Text("E-mail") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = senha,
                    onValueChange = { senha = it; erro = null; sucesso = null },
                    label = { Text("Nova senha (opcional)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                erro?.let { Text(it, color = LumeError) }
                sucesso?.let { Text(it, color = LumeAccent) }
                Button(
                    onClick = {
                        erro = viewModel.atualizarUsuario(nome, email, senha)
                        if (erro == null) {
                            senha = ""
                            sucesso = "Dados atualizados"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LumeAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar alterações", color = LumeBg, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { confirmarExclusao = true }) {
                    Text("Excluir minha conta", color = LumeError)
                }
            }
        }
    }
}

package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.navigation.Screen
import com.example.myapplication.theme.*
import com.example.myapplication.viewmodel.AppViewModel

@Composable
fun CadastroScreen(viewModel: AppViewModel, navController: NavController) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LumeBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CRIAR CONTA",
            color = LumeAccent,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        CampoCadastro("Nome", nome) { nome = it; erro = null }
        Spacer(modifier = Modifier.height(12.dp))
        CampoCadastro("E-mail", email) { email = it; erro = null }
        Spacer(modifier = Modifier.height(12.dp))
        CampoCadastro("Senha", senha, senha = true) { senha = it; erro = null }
        Spacer(modifier = Modifier.height(12.dp))
        CampoCadastro("Confirmar senha", confirmarSenha, senha = true) { confirmarSenha = it; erro = null }

        if (erro != null) {
            Text(
                text = erro!!,
                color = LumeError,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                erro = viewModel.cadastrar(nome, email, senha, confirmarSenha)
                if (erro == null) {
                    // Cadastro já inicia a sessão: vai direto para o Dashboard
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LumeAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cadastrar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LumeBg)
        }

        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Já tenho conta", color = LumeTextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun CampoCadastro(
    label: String,
    valor: String,
    senha: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (senha) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = LumeBorder,
            focusedBorderColor = LumeAccent
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

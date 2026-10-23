package com.example.myapplication.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.AppViewModel

@Composable
fun LoginScreen(
    viewModel: AppViewModel,
    loginSucesso: () -> Unit,
    irCadastro: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("LUME", style = MaterialTheme.typography.headlineLarge)
        Text("Sua vida financeira, simplificada.")
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                viewModel.clearMensagem()
            },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.carregando
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = senha,
            onValueChange = {
                senha = it
                viewModel.clearMensagem()
            },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.carregando
        )

        viewModel.mensagem?.let { mensagem ->
            Spacer(Modifier.height(16.dp))
            Text(mensagem, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.login(email, senha, loginSucesso) },
            enabled = !viewModel.carregando && email.isNotBlank() && senha.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (viewModel.carregando) "Entrando..." else "Entrar")
        }
        TextButton(
            onClick = {
                viewModel.clearMensagem()
                irCadastro()
            },
            enabled = !viewModel.carregando
        ) {
            Text("Criar conta")
        }
    }
}

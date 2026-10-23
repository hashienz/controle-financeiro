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
fun RegisterScreen(
    viewModel: AppViewModel,
    cadastroSucesso: () -> Unit,
    irLogin: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmacao by remember { mutableStateOf("") }
    var erroLocal by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Criar conta", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = {
                nome = it
                erroLocal = null
                viewModel.clearMensagem()
            },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.carregando
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                erroLocal = null
                viewModel.clearMensagem()
            },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.carregando
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = senha,
            onValueChange = {
                senha = it
                erroLocal = null
                viewModel.clearMensagem()
            },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.carregando
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmacao,
            onValueChange = {
                confirmacao = it
                erroLocal = null
                viewModel.clearMensagem()
            },
            label = { Text("Confirmar senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.carregando
        )

        (erroLocal ?: viewModel.mensagem)?.let { mensagem ->
            Spacer(Modifier.height(8.dp))
            Text(mensagem, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (senha != confirmacao) {
                    erroLocal = "As senhas não coincidem."
                } else {
                    viewModel.cadastrar(nome, email, senha, cadastroSucesso)
                }
            },
            enabled = !viewModel.carregando && nome.isNotBlank() &&
                email.isNotBlank() && senha.isNotBlank() && confirmacao.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (viewModel.carregando) "Cadastrando..." else "Cadastrar")
        }
        TextButton(
            onClick = {
                viewModel.clearMensagem()
                irLogin()
            },
            enabled = !viewModel.carregando
        ) {
            Text("Já tenho conta")
        }
    }
}

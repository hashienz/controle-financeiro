package com.example.myapplication.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.view.ExtratoScreen
import com.example.myapplication.view.FormularioScreen
import com.example.myapplication.view.GraficosScreen
import com.example.myapplication.view.HomeScreen
import com.example.myapplication.view.LoginScreen
import com.example.myapplication.view.MetasScreen
import com.example.myapplication.view.RegisterScreen
import com.example.myapplication.view.SecoesScreen
import com.example.myapplication.viewmodel.AppViewModel

@Composable
fun NavegacaoApp(viewModel: AppViewModel) {
    if (viewModel.inicializando) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Carregando...")
        }
        return
    }

    val destinoInicial = remember { if (viewModel.usuario == null) Login else Home }
    val navController = rememberNavController()
    val mostrarMenu = viewModel.usuario != null

    Scaffold(
        topBar = {
            if (mostrarMenu) {
                Column {
                    Menu(navController, viewModel)
                    viewModel.mensagem?.let { mensagem ->
                        Text(mensagem, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = destinoInicial,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Login> {
                LoginScreen(
                    viewModel = viewModel,
                    loginSucesso = {
                        navController.navigate(Home) {
                            popUpTo(Login) { inclusive = true }
                        }
                    },
                    irCadastro = { navController.navigate(Cadastro) }
                )
            }
            composable<Cadastro> {
                RegisterScreen(
                    viewModel = viewModel,
                    cadastroSucesso = {
                        navController.navigate(Home) {
                            popUpTo(Login) { inclusive = true }
                        }
                    },
                    irLogin = { navController.popBackStack() }
                )
            }
            composable<Home> { HomeScreen(viewModel, navController) }
            composable<Secoes> {
                SecoesScreen(
                    irFormulario = { navController.navigate(Formulario) },
                    irExtrato = { navController.navigate(Extrato) },
                    irMetas = { navController.navigate(Metas) },
                    irGraficos = { navController.navigate(Graficos) }
                )
            }
            composable<Extrato> { ExtratoScreen(viewModel) }
            composable<Metas> { MetasScreen(viewModel) }
            composable<Graficos> { GraficosScreen(viewModel) }
            composable<Formulario> { FormularioScreen(viewModel, navController) }
        }
    }
}

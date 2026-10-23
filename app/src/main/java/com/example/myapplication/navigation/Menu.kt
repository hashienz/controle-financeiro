package com.example.myapplication.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.myapplication.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Menu(navController: NavHostController, viewModel: AppViewModel) {
    TopAppBar(
        title = { Text("LUME") },
        actions = {
            TextButton(
                onClick = { navController.navigate(Home) },
                enabled = !viewModel.carregando
            ) {
                Text("Início")
            }
            TextButton(
                onClick = { navController.navigate(Secoes) },
                enabled = !viewModel.carregando
            ) {
                Text("Seções")
            }
            IconButton(
                onClick = {
                    viewModel.sair {
                        navController.navigate(Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                enabled = !viewModel.carregando
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Sair"
                )
            }
        }
    )
}

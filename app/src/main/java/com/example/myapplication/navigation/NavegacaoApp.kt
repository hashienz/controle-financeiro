package com.example.myapplication.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.myapplication.theme.LumeBg
import com.example.myapplication.theme.LumeSurface
import com.example.myapplication.view.*
import com.example.myapplication.viewmodel.AppViewModel

enum class Tela(val titulo: String, val icone: ImageVector) {
    LOGIN("Auth", Icons.Default.Info),
    HOME("Início", Icons.Default.Home),
    EXTRATO("Extrato", Icons.Default.List),
    FORMULARIO("Novo", Icons.Default.Add),
    METAS("Metas", Icons.Default.CheckCircle),
    GRAFICOS("Gráficos", Icons.Default.Info)
}

@Composable
fun NavegacaoApp(viewModel: AppViewModel) {
    var telaAtual by remember { 
        mutableStateOf(if (viewModel.isUserLoggedIn.value) Tela.HOME else Tela.LOGIN) 
    }

    val onNavegar: (Tela) -> Unit = { novaTela ->
        telaAtual = novaTela
    }

    if (telaAtual == Tela.LOGIN) {
        LoginScreen(viewModel = viewModel, onNavegar = onNavegar)
    } else if (telaAtual == Tela.FORMULARIO) {
        FormularioScreen(viewModel = viewModel, onNavegar = onNavegar)
    } else {
        MainScaffold(
            telaAtual = telaAtual,
            onNavegar = onNavegar
        ) {
            when (telaAtual) {
                Tela.HOME -> HomeScreen(viewModel = viewModel, onNavegar = onNavegar)
                Tela.EXTRATO -> ExtratoScreen(viewModel = viewModel)
                Tela.METAS -> MetasScreen(viewModel = viewModel)
                Tela.GRAFICOS -> GraficosScreen(viewModel = viewModel)
                else -> HomeScreen(viewModel = viewModel, onNavegar = onNavegar)
            }
        }
    }
}

@Composable
fun MainScaffold(
    telaAtual: Tela,
    onNavegar: (Tela) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = LumeBg,
        bottomBar = {
            BottomNavigationBar(telaAtual = telaAtual, onNavegar = onNavegar)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}

@Composable
fun BottomNavigationBar(
    telaAtual: Tela,
    onNavegar: (Tela) -> Unit
) {
    val items = listOf(
        Tela.HOME,
        Tela.EXTRATO,
        Tela.METAS,
        Tela.GRAFICOS
    )
    NavigationBar(
        containerColor = LumeBg,
        tonalElevation = 0.dp
    ) {
        items.forEach { tela ->
            NavigationBarItem(
                icon = { Icon(tela.icone, contentDescription = tela.titulo) },
                label = { Text(tela.titulo) },
                selected = telaAtual == tela,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = LumeSurface
                ),
                onClick = { onNavegar(tela) }
            )
        }
    }
}

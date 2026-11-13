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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.myapplication.theme.LumeBg
import com.example.myapplication.theme.LumeSurface
import com.example.myapplication.view.*
import com.example.myapplication.viewmodel.AppViewModel

@Composable
fun NavegacaoApp(viewModel: AppViewModel) {
    val navController = rememberNavController()

    val telaInicial: Any = remember {
        if (viewModel.isUserLoggedIn.value) Home else Login
    }

    NavHost(
        navController = navController,
        startDestination = telaInicial,
        modifier = Modifier.background(LumeBg)
    ) {
        composable<Login> {
            LoginScreen(viewModel = viewModel, navController = navController)
        }
        composable<Cadastro> {
            CadastroScreen(viewModel = viewModel, navController = navController)
        }
        composable<Secoes> {
            SecoesScreen(
                irFormulario = { navController.navigate(Formulario()) },
                irExtrato = { navController.navigate(Extrato) },
                irMetas = { navController.navigate(Metas) },
                irGraficos = { navController.navigate(Graficos) }
            )
        }
        composable<Home> {
            MainScaffold(navController = navController) {
                HomeScreen(viewModel = viewModel, navController = navController)
            }
        }
        composable<Extrato> {
            MainScaffold(navController = navController) {
                ExtratoScreen(viewModel = viewModel, navController = navController)
            }
        }
        composable<Metas> {
            MainScaffold(navController = navController) {
                MetasScreen(viewModel = viewModel)
            }
        }
        composable<Categorias> {
            MainScaffold(navController = navController) {
                CategoriasScreen(viewModel = viewModel)
            }
        }
        composable<Graficos> {
            MainScaffold(navController = navController) {
                GraficosScreen(viewModel = viewModel)
            }
        }
        composable<Formulario> { backStackEntry ->
            val formulario = backStackEntry.toRoute<Formulario>()
            FormularioScreen(
                viewModel = viewModel,
                navController = navController,
                transacaoId = formulario.transacaoId
            )
        }
        composable<Conta> {
            ContaScreen(viewModel = viewModel, navController = navController)
        }
    }
}

@Composable
fun MainScaffold(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = LumeBg,
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        ItemNavegacao(Home, "Início", Icons.Default.Home),
        ItemNavegacao(Extrato, "Extrato", Icons.Default.List),
        ItemNavegacao(Metas, "Metas", Icons.Default.CheckCircle),
        ItemNavegacao(Categorias, "Categorias", Icons.Default.Info),
        ItemNavegacao(Graficos, "Gráficos", Icons.Default.Info)
    )
    NavigationBar(
        containerColor = LumeBg,
        tonalElevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val destination = navBackStackEntry?.destination
        
        items.forEach { item ->
            val selecionado = when (item.rota) {
                Home -> destination?.hasRoute<Home>() == true
                Extrato -> destination?.hasRoute<Extrato>() == true
                Metas -> destination?.hasRoute<Metas>() == true
                Categorias -> destination?.hasRoute<Categorias>() == true
                else -> destination?.hasRoute<Graficos>() == true
            }
            NavigationBarItem(
                icon = { Icon(item.icone, contentDescription = item.titulo) },
                label = { Text(item.titulo) },
                selected = selecionado,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = LumeSurface
                ),
                onClick = {
                    if (!selecionado) {
                        navController.navigate(item.rota) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }

}

private data class ItemNavegacao(
    val rota: Any,
    val titulo: String,
    val icone: ImageVector
)

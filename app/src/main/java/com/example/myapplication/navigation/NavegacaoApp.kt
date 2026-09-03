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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.view.*
import com.example.myapplication.viewmodel.AppViewModel
import com.example.myapplication.ui.theme.*


sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Auth : Screen("auth", "Auth", Icons.Default.Info)
    object Dashboard : Screen("dashboard", "Início", Icons.Default.Home)
    object Extrato : Screen("extrato", "Extrato", Icons.Default.List)
    object Budget : Screen("budget", "Metas", Icons.Default.CheckCircle)
    object Reports : Screen("reports", "Gráficos", Icons.Default.Info)
    object TransactionForm : Screen("transaction_form", "Novo", Icons.Default.Add)
}

@Composable
fun NavegacaoApp(viewModel: AppViewModel) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        // Usa o estado didático do ViewModel
        startDestination = if (viewModel.isUserLoggedIn.value) Screen.Dashboard.route else Screen.Auth.route,
        modifier = Modifier.background(LumeBg)
    ) {
        composable(Screen.Auth.route) {
            LoginScreen(viewModel, navController)
        }
        composable(Screen.Dashboard.route) {
            MainScaffold(navController) {
                HomeScreen(viewModel, navController)
            }
        }
        composable(Screen.Extrato.route) {
            MainScaffold(navController) {
                ExtratoScreen(viewModel)
            }
        }
        composable(Screen.Budget.route) {
            MainScaffold(navController) {
                MetasScreen(viewModel)
            }
        }
        composable(Screen.Reports.route) {
            MainScaffold(navController) {
                GraficosScreen(viewModel)
            }
        }
        composable(Screen.TransactionForm.route) {
            FormularioScreen(viewModel, navController)
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
            BottomNavigationBar(navController)
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
        Screen.Dashboard,
        Screen.Extrato,
        Screen.Budget,
        Screen.Reports
    )
    NavigationBar(
        containerColor = LumeBg,
        tonalElevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = LumeSurface
                ),
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
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

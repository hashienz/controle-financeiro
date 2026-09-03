package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.database.AppDatabase
import com.example.myapplication.repository.AppRepository
import com.example.myapplication.navigation.NavegacaoApp
import com.example.myapplication.viewmodel.AppViewModel
import com.example.myapplication.viewmodel.AppViewModelFactory
import com.example.myapplication.localstorage.PreferenciasApp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar Room e SharedPreferences
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(database.appDao())
        val prefs = PreferenciasApp(applicationContext)
        
        // Inicializar ViewModel usando a Factory customizada
        val factory = AppViewModelFactory(repository, prefs)
        val viewModel = ViewModelProvider(this, factory)[AppViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NavegacaoApp(viewModel)
            }
        }
    }
}

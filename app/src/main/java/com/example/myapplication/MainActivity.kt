package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.localstorage.PreferenciasApp
import com.example.myapplication.navigation.NavegacaoApp
import com.example.myapplication.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.AppViewModel
import com.example.myapplication.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar SharedPreferences e ViewModel
        val prefs = PreferenciasApp(applicationContext)
        val factory = AppViewModelFactory(prefs)
        val viewModel = ViewModelProvider(this, factory)[AppViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NavegacaoApp(viewModel)
            }
        }
    }
}

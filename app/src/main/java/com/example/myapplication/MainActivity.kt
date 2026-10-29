package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.database.CategoriaDAO
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.database.TransacaoDAO
import com.example.myapplication.database.UsuarioDAO
import com.example.myapplication.localstorage.PreferenciasApp
import com.example.myapplication.navigation.NavegacaoApp
import com.example.myapplication.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.AppViewModel
import com.example.myapplication.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // SharedPreferences (sessão) + Banco SQLite + ViewModel
        val prefs = PreferenciasApp(applicationContext)
        val dbHelper = DatabaseHelper(applicationContext)
        val factory = AppViewModelFactory(
            prefs,
            UsuarioDAO(dbHelper),
            CategoriaDAO(dbHelper),
            TransacaoDAO(dbHelper)
        )
        val viewModel = ViewModelProvider(this, factory)[AppViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NavegacaoApp(viewModel)
            }
        }
    }
}

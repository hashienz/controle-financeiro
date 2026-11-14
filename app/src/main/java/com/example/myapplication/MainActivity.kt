package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.database.CategoriaDAO
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.database.MetaDAO
import com.example.myapplication.database.TransacaoDAO
import com.example.myapplication.database.UsuarioDAO
import com.example.myapplication.navigation.NavegacaoApp
import com.example.myapplication.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.AppViewModel
import com.example.myapplication.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Banco SQLite + ViewModel
        val dbHelper = DatabaseHelper(applicationContext)
        val factory = AppViewModelFactory(
            UsuarioDAO(dbHelper),
            CategoriaDAO(dbHelper),
            TransacaoDAO(dbHelper),
            MetaDAO(dbHelper)
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

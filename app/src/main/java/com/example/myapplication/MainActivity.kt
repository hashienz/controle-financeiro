package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.navigation.NavegacaoApp
import com.example.myapplication.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val viewModel = ViewModelProvider(this)[AppViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NavegacaoApp(viewModel)
            }
        }
    }
}

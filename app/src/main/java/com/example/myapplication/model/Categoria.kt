package com.example.myapplication.model

data class Categoria(
    val id: Long = 0,
    val nome: String,
    val iconeNome: String,
    val corHex: String,
    val limiteMensal: Double = 0.0,
    val metaAtiva: Boolean = true
)

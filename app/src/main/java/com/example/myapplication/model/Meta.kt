package com.example.myapplication.model

data class Meta(
    val id: Long = 0,
    val titulo: String,
    val valorAlvo: Double,
    val valorAtual: Double = 0.0,
    val usuarioId: Long = 0
)

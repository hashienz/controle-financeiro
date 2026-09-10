package com.example.myapplication.model

data class Transacao(
    val id: Long = 0,
    val descricao: String,
    val valor: Double,
    val tipo: String, // "RECEITA" ou "DESPESA"
    val dataInMillis: Long,
    val categoriaId: Long,
    val efetivado: Boolean = true
)

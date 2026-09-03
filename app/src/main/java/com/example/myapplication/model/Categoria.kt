package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorias")
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val iconeNome: String,
    val corHex: String,
    val limiteMensal: Double = 0.0,
    val metaAtiva: Boolean = true
)

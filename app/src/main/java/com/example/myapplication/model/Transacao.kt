package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transacoes",
    foreignKeys = [
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TransacaoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val descricao: String,
    val valor: Double,
    val tipo: String, // "RECEITA" ou "DESPESA"
    val dataInMillis: Long,
    val categoriaId: Long,
    val efetivado: Boolean = true
)

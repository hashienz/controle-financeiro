package com.example.myapplication.database

import androidx.room.*
import com.example.myapplication.model.CategoriaEntity
import com.example.myapplication.model.TransacaoEntity

@Dao
interface AppDao {
    @Query("SELECT * FROM transacoes ORDER BY dataInMillis DESC")
    suspend fun getAllTransacoes(): List<TransacaoEntity>

    @Query("SELECT * FROM transacoes ORDER BY dataInMillis DESC LIMIT 5")
    suspend fun getRecentTransacoes(): List<TransacaoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransacao(transacao: TransacaoEntity)

    @Delete
    suspend fun deleteTransacao(transacao: TransacaoEntity)

    @Query("SELECT * FROM categorias")
    suspend fun getAllCategorias(): List<CategoriaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoria(categoria: CategoriaEntity)

    @Update
    suspend fun updateCategoria(categoria: CategoriaEntity)
}

package com.example.myapplication.repository

import com.example.myapplication.database.AppDao
import com.example.myapplication.model.CategoriaEntity
import com.example.myapplication.model.TransacaoEntity

class AppRepository(private val appDao: AppDao) {
    suspend fun getAllTransacoes(): List<TransacaoEntity> {
        return appDao.getAllTransacoes()
    }

    suspend fun getRecentTransacoes(): List<TransacaoEntity> {
        return appDao.getRecentTransacoes()
    }

    suspend fun insertTransacao(transacao: TransacaoEntity) {
        appDao.insertTransacao(transacao)
    }

    suspend fun deleteTransacao(transacao: TransacaoEntity) {
        appDao.deleteTransacao(transacao)
    }

    suspend fun getAllCategorias(): List<CategoriaEntity> {
        return appDao.getAllCategorias()
    }

    suspend fun insertCategoria(categoria: CategoriaEntity) {
        appDao.insertCategoria(categoria)
    }

    suspend fun updateCategoria(categoria: CategoriaEntity) {
        appDao.updateCategoria(categoria)
    }
}

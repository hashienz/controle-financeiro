package com.example.myapplication.database

import android.content.ContentValues
import com.example.myapplication.model.Transacao

// CRUD completo de Transação
class TransacaoDAO(private val dbHelper: DatabaseHelper) {

    // CREATE
    fun inserir(transacao: Transacao): Long {
        val db = dbHelper.writableDatabase
        val id = db.insert(DatabaseHelper.TABELA_TRANSACAO, null, toContentValues(transacao))
        db.close()
        return id
    }

    // READ (listar) - mais recentes primeiro
    fun listarPorUsuario(usuarioId: Long): List<Transacao> {
        val lista = mutableListOf<Transacao>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABELA_TRANSACAO} WHERE usuario_id = ? ORDER BY data DESC",
            arrayOf(usuarioId.toString())
        )
        while (cursor.moveToNext()) {
            lista.add(
                Transacao(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao")),
                    valor = cursor.getDouble(cursor.getColumnIndexOrThrow("valor")),
                    tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo")),
                    dataInMillis = cursor.getLong(cursor.getColumnIndexOrThrow("data")),
                    categoriaId = cursor.getLong(cursor.getColumnIndexOrThrow("categoria_id")),
                    efetivado = cursor.getInt(cursor.getColumnIndexOrThrow("efetivado")) == 1,
                    usuarioId = cursor.getLong(cursor.getColumnIndexOrThrow("usuario_id")),
                    localizacao = cursor.getColumnIndex("localizacao").let { idx ->
                        if (idx >= 0) cursor.getString(idx) ?: "" else ""
                    }
                )
            )
        }
        cursor.close()
        db.close()
        return lista
    }

    // UPDATE
    fun atualizar(transacao: Transacao): Int {
        val db = dbHelper.writableDatabase
        val linhas = db.update(
            DatabaseHelper.TABELA_TRANSACAO,
            toContentValues(transacao),
            "id = ?",
            arrayOf(transacao.id.toString())
        )
        db.close()
        return linhas
    }

    // DELETE
    fun deletar(id: Long): Int {
        val db = dbHelper.writableDatabase
        val linhas = db.delete(DatabaseHelper.TABELA_TRANSACAO, "id = ?", arrayOf(id.toString()))
        db.close()
        return linhas
    }

    // Usado quando uma categoria é apagada: remove as transações ligadas a ela
    fun deletarPorCategoria(categoriaId: Long): Int {
        val db = dbHelper.writableDatabase
        val linhas = db.delete(
            DatabaseHelper.TABELA_TRANSACAO,
            "categoria_id = ?",
            arrayOf(categoriaId.toString())
        )
        db.close()
        return linhas
    }

    private fun toContentValues(transacao: Transacao): ContentValues {
        return ContentValues().apply {
            put("descricao", transacao.descricao)
            put("valor", transacao.valor)
            put("tipo", transacao.tipo)
            put("data", transacao.dataInMillis)
            put("categoria_id", transacao.categoriaId)
            put("efetivado", if (transacao.efetivado) 1 else 0)
            put("usuario_id", transacao.usuarioId)
            put("localizacao", transacao.localizacao)
        }
    }
}

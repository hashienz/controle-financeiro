package com.example.myapplication.database

import android.content.ContentValues
import com.example.myapplication.model.Meta

class MetaDAO(private val dbHelper: DatabaseHelper) {

    fun inserir(meta: Meta): Long {
        val db = dbHelper.writableDatabase
        val id = db.insert(DatabaseHelper.TABELA_META, null, meta.toContentValues())
        db.close()
        return id
    }

    fun listarPorUsuario(usuarioId: Long): List<Meta> {
        val lista = mutableListOf<Meta>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABELA_META} WHERE usuario_id = ? ORDER BY titulo",
            arrayOf(usuarioId.toString())
        )
        while (cursor.moveToNext()) {
            lista.add(
                Meta(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo")),
                    valorAlvo = cursor.getDouble(cursor.getColumnIndexOrThrow("valor_alvo")),
                    valorAtual = cursor.getDouble(cursor.getColumnIndexOrThrow("valor_atual")),
                    usuarioId = cursor.getLong(cursor.getColumnIndexOrThrow("usuario_id"))
                )
            )
        }
        cursor.close()
        db.close()
        return lista
    }

    fun atualizar(meta: Meta): Int {
        val db = dbHelper.writableDatabase
        val linhas = db.update(
            DatabaseHelper.TABELA_META,
            meta.toContentValues(),
            "id = ? AND usuario_id = ?",
            arrayOf(meta.id.toString(), meta.usuarioId.toString())
        )
        db.close()
        return linhas
    }

    fun deletar(id: Long, usuarioId: Long): Int {
        val db = dbHelper.writableDatabase
        val linhas = db.delete(
            DatabaseHelper.TABELA_META,
            "id = ? AND usuario_id = ?",
            arrayOf(id.toString(), usuarioId.toString())
        )
        db.close()
        return linhas
    }

    fun deletarPorUsuario(usuarioId: Long): Int {
        val db = dbHelper.writableDatabase
        val linhas = db.delete(
            DatabaseHelper.TABELA_META,
            "usuario_id = ?",
            arrayOf(usuarioId.toString())
        )
        db.close()
        return linhas
    }

    private fun Meta.toContentValues(): ContentValues {
        return ContentValues().apply {
            put("titulo", titulo)
            put("valor_alvo", valorAlvo)
            put("valor_atual", valorAtual)
            put("usuario_id", usuarioId)
        }
    }
}

package com.example.myapplication.database

import android.content.ContentValues
import com.example.myapplication.model.Categoria

// CRUD completo de Categoria
class CategoriaDAO(private val dbHelper: DatabaseHelper) {

    // CREATE
    fun inserir(categoria: Categoria): Long {
        val db = dbHelper.writableDatabase
        val id = db.insert(DatabaseHelper.TABELA_CATEGORIA, null, toContentValues(categoria))
        db.close()
        return id
    }

    // READ (listar)
    fun listarPorUsuario(usuarioId: Long): List<Categoria> {
        val lista = mutableListOf<Categoria>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABELA_CATEGORIA} WHERE usuario_id = ? ORDER BY nome",
            arrayOf(usuarioId.toString())
        )
        while (cursor.moveToNext()) {
            lista.add(
                Categoria(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                    iconeNome = cursor.getString(cursor.getColumnIndexOrThrow("icone")),
                    corHex = cursor.getString(cursor.getColumnIndexOrThrow("cor")),
                    limiteMensal = cursor.getDouble(cursor.getColumnIndexOrThrow("limite_mensal")),
                    metaAtiva = cursor.getInt(cursor.getColumnIndexOrThrow("meta_ativa")) == 1,
                    usuarioId = cursor.getLong(cursor.getColumnIndexOrThrow("usuario_id"))
                )
            )
        }
        cursor.close()
        db.close()
        return lista
    }

    // UPDATE
    fun atualizar(categoria: Categoria): Int {
        val db = dbHelper.writableDatabase
        val linhas = db.update(
            DatabaseHelper.TABELA_CATEGORIA,
            toContentValues(categoria),
            "id = ?",
            arrayOf(categoria.id.toString())
        )
        db.close()
        return linhas
    }

    // DELETE
    fun deletar(id: Long): Int {
        val db = dbHelper.writableDatabase
        val linhas = db.delete(DatabaseHelper.TABELA_CATEGORIA, "id = ?", arrayOf(id.toString()))
        db.close()
        return linhas
    }

    fun deletarPorUsuario(usuarioId: Long): Int {
        val db = dbHelper.writableDatabase
        val linhas = db.delete(
            DatabaseHelper.TABELA_CATEGORIA,
            "usuario_id = ?",
            arrayOf(usuarioId.toString())
        )
        db.close()
        return linhas
    }

    private fun toContentValues(categoria: Categoria): ContentValues {
        return ContentValues().apply {
            put("nome", categoria.nome)
            put("icone", categoria.iconeNome)
            put("cor", categoria.corHex)
            put("limite_mensal", categoria.limiteMensal)
            put("meta_ativa", if (categoria.metaAtiva) 1 else 0)
            put("usuario_id", categoria.usuarioId)
        }
    }
}

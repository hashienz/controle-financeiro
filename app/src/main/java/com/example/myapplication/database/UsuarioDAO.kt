package com.example.myapplication.database

import android.content.ContentValues
import com.example.myapplication.model.Usuario

class UsuarioDAO(private val dbHelper: DatabaseHelper) {

    // Cadastra um usuário. Retorna o id gerado, ou -1 se der erro (ex.: e-mail repetido)
    fun inserir(usuario: Usuario): Long {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("nome", usuario.nome)
            put("email", usuario.email)
            put("senha", usuario.senha)
        }
        val id = db.insert(DatabaseHelper.TABELA_USUARIO, null, valores)
        db.close()
        return id
    }

    fun buscarPorEmail(email: String): Usuario? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABELA_USUARIO} WHERE email = ?",
            arrayOf(email)
        )
        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = Usuario(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                senha = cursor.getString(cursor.getColumnIndexOrThrow("senha"))
            )
        }
        cursor.close()
        db.close()
        return usuario
    }

    // Usado no login: só retorna o usuário se e-mail e senha baterem
    fun autenticar(email: String, senha: String): Usuario? {
        val usuario = buscarPorEmail(email)
        return if (usuario != null && usuario.senha == senha) usuario else null
    }
}

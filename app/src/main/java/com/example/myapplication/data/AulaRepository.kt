package com.example.myapplication.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Patterns
import com.example.myapplication.auth.AuthManager
import com.example.myapplication.model.Transacao
import java.io.File

data class Usuario(val id: Long, val nome: String)

/** Os métodos acessam o banco e devem ser chamados em Dispatchers.IO. */
class AulaRepository(context: Context) {
    private val appContext = context.applicationContext

    fun sessaoAtual(): Usuario? = AppDatabase(appContext).use { helper ->
        val db = helper.readableDatabase
        val sessao = db.query(
            "eventos_sessao",
            arrayOf("usuario_id", "ativo"),
            null, null, null, null, "id DESC"
        ).use { cursor ->
            if (cursor.moveToFirst() && cursor.getInt(1) == 1) cursor.getLong(0) else null
        }
        if (sessao == null) null else buscarUsuario(db, sessao)
    }

    fun cadastrar(nome: String, email: String, senha: String): Usuario? {
        val nomeLimpo = nome.trim()
        val emailLimpo = email.trim().lowercase()
        require(nomeLimpo.length in 3..100) { "Informe um nome entre 3 e 100 caracteres." }
        require(emailLimpo.length <= 254 && Patterns.EMAIL_ADDRESS.matcher(emailLimpo).matches()) {
            "Informe um e-mail válido."
        }
        require(senha.length in 6..128 && senha.isNotBlank()) {
            "Use uma senha entre 6 e 128 caracteres."
        }

        return AppDatabase(appContext).use { helper ->
            val db = helper.writableDatabase
            if (emailCadastrado(db, emailLimpo)) return@use null

            val salt = AuthManager.criarSalt()
            val hash = AuthManager.gerarHash(senha, salt)
            try {
                val dados = ContentValues().apply {
                    put("nome", nomeLimpo)
                    put("email", emailLimpo)
                    put("senha", hash)
                    put("salt", salt)
                }
                val id = try {
                    db.insertOrThrow("usuarios", null, dados)
                } catch (_: SQLiteConstraintException) {
                    return@use null
                }
                registrarSessao(db, id, true)
                Usuario(id, nomeLimpo)
            } finally {
                hash.fill(0)
            }
        }
    }

    fun entrar(email: String, senha: String): Usuario? = AppDatabase(appContext).use { helper ->
        val db = helper.writableDatabase
        val dados = buscarSenha(db, email.trim().lowercase())
        val hash = dados?.hash ?: ByteArray(32)
        val salt = dados?.salt ?: ByteArray(16)
        try {
            if (!AuthManager.validarSenha(senha, salt, hash) || dados == null) return@use null
            registrarSessao(db, dados.usuario.id, true)
            dados.usuario
        } finally {
            hash.fill(0)
        }
    }

    fun sair(usuarioId: Long) {
        AppDatabase(appContext).use { helper ->
            val db = helper.writableDatabase
            val atual = db.query(
                "eventos_sessao",
                arrayOf("usuario_id", "ativo"),
                null, null, null, null, "id DESC"
            ).use { cursor ->
                if (cursor.moveToFirst() && cursor.getInt(1) == 1) cursor.getLong(0) else null
            }
            if (atual == usuarioId) registrarSessao(db, usuarioId, false)
        }
    }

    fun listarTransacoes(usuarioId: Long): List<Transacao> = AppDatabase(appContext).use { helper ->
        helper.readableDatabase.query(
            "transacoes",
            arrayOf("id", "descricao", "valor", "tipo", "data_millis", "categoria_id", "efetivado"),
            "usuario_id = ?",
            arrayOf(usuarioId.toString()),
            null, null, "id DESC"
        ).use { cursor ->
            val transacoes = mutableListOf<Transacao>()
            while (cursor.moveToNext()) {
                transacoes.add(
                    Transacao(
                        id = cursor.getLong(0),
                        descricao = cursor.getString(1),
                        valor = cursor.getDouble(2),
                        tipo = cursor.getString(3),
                        dataInMillis = cursor.getLong(4),
                        categoriaId = cursor.getLong(5),
                        efetivado = cursor.getInt(6) == 1
                    )
                )
            }
            transacoes
        }
    }

    fun inserirTransacao(
        usuarioId: Long,
        descricao: String,
        valor: Double,
        tipo: String,
        categoriaId: Long,
        efetivado: Boolean
    ) {
        require(valor > 0.0 && valor.isFinite()) { "Informe um valor positivo." }
        require(tipo == "RECEITA" || tipo == "DESPESA") { "Tipo de lançamento inválido." }
        AppDatabase(appContext).use { helper ->
            val dados = ContentValues().apply {
                put("usuario_id", usuarioId)
                put("descricao", descricao)
                put("valor", valor)
                put("tipo", tipo)
                put("data_millis", System.currentTimeMillis())
                put("categoria_id", categoriaId)
                put("efetivado", if (efetivado) 1 else 0)
            }
            helper.writableDatabase.insertOrThrow("transacoes", null, dados)
        }
    }

    fun limiteAtual(usuarioId: Long, categoriaId: Long): Double? = AppDatabase(appContext).use { helper ->
        helper.readableDatabase.query(
            "historico_limites",
            arrayOf("limite"),
            "usuario_id = ? AND categoria_id = ?",
            arrayOf(usuarioId.toString(), categoriaId.toString()),
            null, null, "id DESC"
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getDouble(0) else null
        }
    }

    fun salvarLimite(usuarioId: Long, categoriaId: Long, limite: Double) {
        require(limite.isFinite()) { "Informe um limite válido." }
        AppDatabase(appContext).use { helper ->
            val dados = ContentValues().apply {
                put("usuario_id", usuarioId)
                put("categoria_id", categoriaId)
                put("limite", limite)
            }
            helper.writableDatabase.insertOrThrow("historico_limites", null, dados)
        }
    }

    private fun registrarSessao(db: SQLiteDatabase, usuarioId: Long, ativo: Boolean) {
        val dados = ContentValues().apply {
            put("usuario_id", usuarioId)
            put("ativo", if (ativo) 1 else 0)
        }
        db.insertOrThrow("eventos_sessao", null, dados)
    }

    private fun buscarUsuario(db: SQLiteDatabase, id: Long): Usuario? = db.query(
        "usuarios",
        arrayOf("id", "nome"),
        "id = ?",
        arrayOf(id.toString()),
        null, null, null
    ).use { cursor ->
        if (cursor.moveToFirst()) Usuario(cursor.getLong(0), cursor.getString(1)) else null
    }

    private fun emailCadastrado(db: SQLiteDatabase, email: String): Boolean = db.query(
        "usuarios",
        arrayOf("id"),
        "email = ?",
        arrayOf(email),
        null, null, null
    ).use { cursor ->
        cursor.moveToFirst()
    }

    private fun buscarSenha(db: SQLiteDatabase, email: String): SenhaSalva? = db.query(
        "usuarios",
        arrayOf("id", "nome", "senha", "salt"),
        "email = ?",
        arrayOf(email),
        null, null, null
    ).use { cursor ->
        if (cursor.moveToFirst()) {
            SenhaSalva(
                Usuario(cursor.getLong(0), cursor.getString(1)),
                cursor.getBlob(2),
                cursor.getBlob(3)
            )
        } else null
    }

    private class SenhaSalva(val usuario: Usuario, val hash: ByteArray, val salt: ByteArray)
}

private class AppDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    File(context.noBackupFilesDir, "controle_financeiro_aula.db").absolutePath,
    null,
    1
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE usuarios (
                id INTEGER PRIMARY KEY,
                nome TEXT NOT NULL CHECK(length(nome) BETWEEN 3 AND 100),
                email TEXT NOT NULL UNIQUE CHECK(length(email) BETWEEN 3 AND 254),
                senha BLOB NOT NULL,
                salt BLOB NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE eventos_sessao (
                id INTEGER PRIMARY KEY,
                usuario_id INTEGER NOT NULL,
                ativo INTEGER NOT NULL CHECK(ativo IN (0, 1))
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE transacoes (
                id INTEGER PRIMARY KEY,
                usuario_id INTEGER NOT NULL,
                descricao TEXT NOT NULL,
                valor REAL NOT NULL,
                tipo TEXT NOT NULL,
                data_millis INTEGER NOT NULL,
                categoria_id INTEGER NOT NULL,
                efetivado INTEGER NOT NULL CHECK(efetivado IN (0, 1))
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE historico_limites (
                id INTEGER PRIMARY KEY,
                usuario_id INTEGER NOT NULL,
                categoria_id INTEGER NOT NULL,
                limite REAL NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        error("Migração do banco necessária da versão $oldVersion para $newVersion")
    }
}

package com.example.myapplication.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Classe responsável por criar e atualizar o banco SQLite do app
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, NOME_BANCO, null, VERSAO_BANCO) {

    companion object {
        const val NOME_BANCO = "lume.db"
        const val VERSAO_BANCO = 1

        const val TABELA_USUARIO = "usuario"
        const val TABELA_CATEGORIA = "categoria"
        const val TABELA_TRANSACAO = "transacao"
    }

    // Chamado só na primeira vez que o banco é aberto: cria as tabelas
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABELA_USUARIO (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                senha TEXT NOT NULL
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE $TABELA_CATEGORIA (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                icone TEXT NOT NULL,
                cor TEXT NOT NULL,
                limite_mensal REAL NOT NULL DEFAULT 0,
                meta_ativa INTEGER NOT NULL DEFAULT 1,
                usuario_id INTEGER NOT NULL
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE $TABELA_TRANSACAO (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                descricao TEXT NOT NULL,
                valor REAL NOT NULL,
                tipo TEXT NOT NULL,
                data INTEGER NOT NULL,
                categoria_id INTEGER NOT NULL,
                efetivado INTEGER NOT NULL DEFAULT 1,
                usuario_id INTEGER NOT NULL
            )
            """
        )
    }

    // Chamado quando VERSAO_BANCO aumenta: apaga e recria as tabelas
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABELA_TRANSACAO")
        db.execSQL("DROP TABLE IF EXISTS $TABELA_CATEGORIA")
        db.execSQL("DROP TABLE IF EXISTS $TABELA_USUARIO")
        onCreate(db)
    }
}

package com.example.myapplication.localstorage

import android.content.Context
import android.content.SharedPreferences

// Controle de sessão: guarda qual usuário está logado
class PreferenciasApp(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lume_prefs", Context.MODE_PRIVATE)

    fun salvarSessao(usuarioId: Long, nome: String) {
        prefs.edit()
            .putLong("usuario_id", usuarioId)
            .putString("usuario_nome", nome)
            .apply()
    }

    fun getUsuarioId(): Long {
        return prefs.getLong("usuario_id", -1L)
    }

    fun getNomeUsuario(): String {
        return prefs.getString("usuario_nome", "") ?: ""
    }

    fun isLoggedIn(): Boolean {
        return getUsuarioId() != -1L
    }

    fun limparSessao() {
        prefs.edit().clear().apply()
    }
}

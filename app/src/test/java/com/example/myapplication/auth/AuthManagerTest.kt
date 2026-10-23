package com.example.myapplication.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthManagerTest {
    @Test
    fun senhaCorretaEIncorreta() {
        val salt = AuthManager.criarSalt()
        val hash = AuthManager.gerarHash("senha de teste", salt)

        assertTrue(AuthManager.validarSenha("senha de teste", salt, hash))
        assertFalse(AuthManager.validarSenha("outra senha", salt, hash))
    }

    @Test
    fun saltsDiferentesGeramHashesDiferentes() {
        val primeiroSalt = AuthManager.criarSalt()
        val segundoSalt = AuthManager.criarSalt()
        val primeiroHash = AuthManager.gerarHash("senha de teste", primeiroSalt)
        val segundoHash = AuthManager.gerarHash("senha de teste", segundoSalt)

        assertFalse(primeiroHash.contentEquals(segundoHash))
    }
}

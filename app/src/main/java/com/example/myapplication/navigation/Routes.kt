package com.example.myapplication.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Login

@Serializable
data object Cadastro

@Serializable
data object Home

@Serializable
data object Secoes

@Serializable
data object Extrato

@Serializable
data object Metas

@Serializable
data object Categorias

@Serializable
data object Conta

@Serializable
data object Graficos

@Serializable
data class Formulario(val transacaoId: Long? = null)

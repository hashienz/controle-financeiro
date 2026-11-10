package com.example.myapplication.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class MoedaCotacao(
    val code: String = "",
    val name: String = "",
    val bid: String = "0.0",
    val pctChange: String = "0.0"
)

data class CotacoesResponse(
    val USDBRL: MoedaCotacao? = null,
    val EURBRL: MoedaCotacao? = null,
    val BTCBRL: MoedaCotacao? = null
)

interface CotacaoApiService {
    @GET("last/USD-BRL,EUR-BRL,BTC-BRL")
    suspend fun buscarCotacoes(): CotacoesResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://economia.awesomeapi.com.br/"

    val api: CotacaoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CotacaoApiService::class.java)
    }
}

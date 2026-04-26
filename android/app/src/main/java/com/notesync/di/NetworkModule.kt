package com.notesync.di

import com.notesync.data.remote.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    // In sviluppo usiamo 10.0.2.2 per raggiungere localhost dall'emulatore.
    // Su un dispositivo fisico usa l'IP locale del computer (es. 192.168.1.x).
    val BASE_URL = "http://10.0.2.2:8080/"
    // single: OkHttpClient — il client HTTP di basso livello.
    // Aggiunge logging di tutte le richieste/risposte (utile per debug).
    single {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    // BODY logga header + body completo di ogni richiesta.
                    // In produzione usa Level.NONE per performance e privacy.
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }
    // single: Retrofit — usa OkHttpClient costruito sopra tramite get().
    // GsonConverterFactory converte automaticamente JSON <-> oggetti Kotlin.
    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get()) // get() recupera OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    // single: ApiService — Retrofit crea l'implementazione a runtime.
    single {
        get<Retrofit>().create(ApiService::class.java)
    }
}
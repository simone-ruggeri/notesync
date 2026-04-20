package com.notesync.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // In sviluppo usiamo 10.0.2.2 invece di localhost:
    // l'emulatore Android vede il tuo computer su questo IP
    // Su un dispositivo fisico, usa l'IP locale del tuo computer (es. 192.168.1.x)
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // OkHttpClient è il client HTTP sottostante
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        // HttpLoggingInterceptor logga tutte le richieste e risposte HTTP
        // BODY mostra il contenuto JSON — utile per debugging, disabilitare in produzione
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val apiService: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create()) // JSON ↔ Kotlin objects
        .build()
        .create(ApiService::class.java)
}
package com.notesync.di

import com.notesync.data.remote.ApiService
import com.notesync.data.remote.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    val BASE_URL = "http://10.0.2.2:8080/"

    single { AuthInterceptor(get()) }

    single {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(get<AuthInterceptor>())
            .addInterceptor(
                HttpLoggingInterceptor().apply {
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
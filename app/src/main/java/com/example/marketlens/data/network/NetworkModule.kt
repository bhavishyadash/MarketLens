package com.example.marketlens.data.network

import com.example.marketlens.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {

    private const val BASE_URL = "https://finnhub.io/api/v1/"

    // Appends ?token=YOUR_KEY to every request automatically
    private val tokenInterceptor = Interceptor { chain ->
        val urlWithToken = chain.request().url.newBuilder()
            .addQueryParameter("token", BuildConfig.FINNHUB_API_KEY)
            .build()
        chain.proceed(chain.request().newBuilder().url(urlWithToken).build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(tokenInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val marketApi: MarketApi = retrofit.create(MarketApi::class.java)
}
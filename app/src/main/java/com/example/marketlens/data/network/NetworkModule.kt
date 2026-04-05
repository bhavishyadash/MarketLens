package com.example.marketlens.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val yahooClient = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
                .addHeader("Accept", "application/json")
                .addHeader("Connection", "keep-alive")
                .build()
            chain.proceed(request)
        })
        .build()

    val yahooFinanceApi: YahooFinanceApi = Retrofit.Builder()
        .baseUrl("https://query2.finance.yahoo.com/")
        .client(yahooClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(YahooFinanceApi::class.java)
}
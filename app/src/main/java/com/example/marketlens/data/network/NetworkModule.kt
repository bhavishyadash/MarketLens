package com.example.marketlens.data.network

import com.example.marketlens.BuildConfig
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

    private val finnhubClient = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            val original = chain.request()
            val url = original.url.newBuilder()
                .addQueryParameter("token", BuildConfig.FINNHUB_API_KEY)
                .build()
            chain.proceed(original.newBuilder().url(url).build())
        })
        .build()

    val marketApi: MarketApi = Retrofit.Builder()
        .baseUrl("https://finnhub.io/api/v1/")
        .client(finnhubClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(MarketApi::class.java)

    private val yahooClient = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                )
                .build()
            chain.proceed(request)
        })
        .build()

    val yahooFinanceApi: YahooFinanceApi = Retrofit.Builder()
        .baseUrl("https://query1.finance.yahoo.com/")
        .client(yahooClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(YahooFinanceApi::class.java)
}
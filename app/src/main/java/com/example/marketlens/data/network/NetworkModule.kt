package com.example.marketlens.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/*
    Bug fixed: package was "com.example.marketlens.network"

    NetworkModule is a singleton object — there is exactly one instance for
    the whole app lifetime. It builds the HTTP client and Retrofit once,
    then vends a ready-to-use MarketApi.

    Why singleton? Building OkHttpClient and Retrofit is expensive.
    You never want to recreate them on every screen.
    (In Phase 2 we'll use Hilt for dependency injection instead.)
*/
object NetworkModule {

    // Finnhub free-tier base URL. Every API call is appended to this.
    private const val BASE_URL = "https://finnhub.io/api/v1/"

    // Logs every HTTP request/response to Logcat in debug builds.
    // BASIC = just the method + URL + status code (not the full body).
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // The HTTP client. Interceptors run on every request.
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Moshi converts JSON ↔ Kotlin data classes.
    // KotlinJsonAdapterFactory handles Kotlin-specific things like default params.
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Retrofit ties the base URL + HTTP client + JSON converter together.
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    // The actual API interface. Call NetworkModule.marketApi.getQuote("AAPL", key)
    val marketApi: MarketApi = retrofit.create(MarketApi::class.java)
}

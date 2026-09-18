package com.example.marketlens.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val yahooCookieJar = InMemoryCookieJar()

    private val yahooClient = OkHttpClient.Builder()
        .cookieJar(yahooCookieJar)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36"
                )
                .addHeader("Accept", "application/json,text/plain,*/*")
                .addHeader("Accept-Language", "en-US,en;q=0.9")
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

    val yahooFinanceSession: YahooCrumbProvider by lazy {
        YahooFinanceSession(client = yahooClient, cookieJar = yahooCookieJar)
    }
}

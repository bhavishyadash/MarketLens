package com.example.marketlens.data.network

import com.example.marketlens.data.network.dto.YahooChartResponseDto
import com.example.marketlens.data.network.dto.YahooQuoteResponseDto
import com.example.marketlens.data.network.dto.YahooSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface YahooFinanceApi {

    /*
        GET /v8/finance/chart/AAPL?interval=1d&range=1mo
        Historical price chart — already in use, unchanged.
    */
    @GET("v8/finance/chart/{symbol}")
    suspend fun getChart(
        @Path("symbol")    symbol: String,
        @Query("interval") interval: String,
        @Query("range")    range: String
    ): YahooChartResponseDto

    /*
        GET /v7/finance/quote?symbols=AAPL,MSFT,NVDA
        Real-time quote for one or multiple symbols.
        One call replaces all Finnhub quote + profile + metric calls.

        symbols = comma-separated list, e.g. "AAPL,MSFT,NVDA"
    */
    @GET("v7/finance/quote")
    suspend fun getQuotes(
        @Query("symbols") symbols: String
    ): YahooQuoteResponseDto

    /*
        GET /v1/finance/search?q=apple&quotesCount=10&newsCount=0
        Dual-purpose endpoint:
          - Symbol search:  q=query,  quotesCount=10, newsCount=0
          - News fetch:     q=symbol, quotesCount=0,  newsCount=20
    */
    @GET("v1/finance/search")
    suspend fun search(
        @Query("q")            query: String,
        @Query("quotesCount")  quotesCount: Int = 10,
        @Query("newsCount")    newsCount: Int = 0
    ): YahooSearchResponseDto
}

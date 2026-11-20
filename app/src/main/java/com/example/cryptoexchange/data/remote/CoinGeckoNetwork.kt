package com.example.cryptoexchange.data.remote

import com.example.cryptoexchange.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CoinGeckoNetwork {

    private const val BASE_URL = "https://api.coingecko.com/api/v3/"
    private const val API_KEY_HEADER = "x-cg-demo-api-key"


    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val builder = chain.request().newBuilder()
            val key = BuildConfig.COINGECKO_API_KEY
            if (key.isNotBlank()) {
                builder.header(API_KEY_HEADER, key)
            }
            chain.proceed(builder.build())
        }
        .build()

    val api: CoinGeckoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CoinGeckoApi::class.java)
    }
}
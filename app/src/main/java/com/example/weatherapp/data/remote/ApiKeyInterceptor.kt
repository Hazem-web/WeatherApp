package com.example.weatherapp.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalUrl = original.url()


        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("appid", "09e813575e7fe73903875144e22aa29d")
            .build()

        val newRequest = original.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}
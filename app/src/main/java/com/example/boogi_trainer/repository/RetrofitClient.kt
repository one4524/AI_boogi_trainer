package com.example.boogi_trainer.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
  private val retrofitClient: Retrofit.Builder by lazy{
      Retrofit.Builder()
          .baseUrl(API.DOMAIN)
          .addConverterFactory(GsonConverterFactory.create())
  }
    val restAPI: RestAPI by lazy{
        retrofitClient.build().create(RestAPI::class.java)
    }
}





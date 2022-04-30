package com.example.boogi_trainer.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface APIS{

//    @GET("/app/AppSessionList")
//    fun session_list(
//        @Header("token") token: String
//    ): Call<com.tta.ttavote.SessionListAns>

    @POST("/app/AppMemberInfo")
    fun member_info(
        @Header("token") token: String,
        @Body jsonparams: com.example.boogi_trainer.data.MemberReq
    ): Call<com.example.boogi_trainer.data.MemberAns>





    companion object { // static 처럼 공유객체로 사용가능함. 모든 인스턴스가 공유하는 객체로서 동작함.
        private const val BASE_URL = "http://112.146.120.145:3010" // 주소

        fun create(): com.example.boogi_trainer.data.APIS {


            val gson : Gson = GsonBuilder().setLenient().create();

            return Retrofit.Builder()
                .baseUrl(com.example.boogi_trainer.data.APIS.Companion.BASE_URL)
//                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(com.example.boogi_trainer.data.APIS::class.java)
        }
    }
}
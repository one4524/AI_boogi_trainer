package com.example.boogi_trainer.repository

import retrofit2.Call
import retrofit2.Response


class APIManager {
    companion object{
        private val caller = RetrofitClient.restAPI
        fun getUser(uid:String): User? {
            var data = caller.getUser(uid).execute().body()

            return data?.user
        }

        fun getUserLog(uid:String): UserLog? {
            var data = caller.getUserLog(uid).execute().body()
            return data?.userLog
        }
/*
        fun getUser(uid:String): User? {
            var user: User? = null
            caller.getUser(uid).enqueue(object:retrofit2.Callback<ResponseUser> {
                override fun onResponse(call: Call<ResponseUser>, response: Response<ResponseUser>){
                    if(response.isSuccessful) { // <--> response.code == 200
                        user = response.body()?.user?.copy()
                        println(2)
                        println(user)
                        println(user)
                    } else { // code == 400
                        println("실패")
                    }
                }
                override fun onFailure(call: Call<ResponseUser>, t: Throwable) {
                    println("실패")
                }
            })
            return user
        }



        fun getUserLog(uid:String){
            caller.getUserLog(uid).enqueue(object:retrofit2.Callback<ResponseUserLog> {
                override fun onResponse(call: Call<ResponseUserLog>, response: Response<ResponseUserLog>) {
                    if(response.isSuccessful) { // <--> response.code == 200
                       userLog = response.body()?.userLog
                    } else { // code == 400
                        println("400에러")
                    }
                }
                override fun onFailure(call: Call<ResponseUserLog>, t: Throwable) {
                    println(t.localizedMessage)
                    println("실패")
                }
            })
        }
        */
    }
}
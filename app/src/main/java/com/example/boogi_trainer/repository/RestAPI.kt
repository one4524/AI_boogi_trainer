package com.example.boogi_trainer.repository

import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*
data class ResponseUser(val user:User?)
data class ResponseUserLog(val userLog:UserLog?)

data class User(var uid: String?,
                @SerializedName("body_form")
                var bodyForm:String?,
                var height:Float?,
                var name: String?,
                var password:String?,
                var weight:Float?)
data class UserLog(val uid: String?,
                   val dates:ArrayList<DateLog>?)

data class DateLog(val date: String?,
                val exercises:ArrayList<Exercise>?,
                val meals:ArrayList<Food>?,
                @SerializedName("diet_info")
                val dietInfo: DietInfo?
)
data class Exercise(
    val exercise: String?,
    val reps: Int?
)
data class Food(
    val food: String?,
    val size: String?,
    val kind: String?
)
data class DietInfo(
    @SerializedName("intake_kcal")
    val intakeKcal: Float?,
    @SerializedName("burned_kcal")
    val burnedKcal: Float?,
    @SerializedName("exercise_time")
    val exerciseTime: Int?,
    val weight: Float?,
    @SerializedName("intake_carbs")
    val intakeCarbs: Float?,
    @SerializedName("intake_protein")
    val intakeProtein: Float?,
    @SerializedName("intake_fat")
    val intakeFat: Float?
)

interface RestAPI {
    @GET("userLogs/{uid}")
    fun getUserLog(@Path("uid") uid:String):Call<ResponseUserLog>


    @GET("users/{uid}")
    fun getUser(@Path("uid") uid:String):Call<ResponseUser>
}
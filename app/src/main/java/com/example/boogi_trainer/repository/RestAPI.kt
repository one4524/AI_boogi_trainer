package com.example.boogi_trainer.repository

import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*


enum class ExerciseType{
    PUSH_UP, SQUAT, PULL_UP, SIT_UP, DEAD_LIFT, BARBELL_ROW, DUMBBELL_CURL,BARBELL_CURL, PLANK
}
enum class CardioExerciseType{
    RUNNING_MACHINE, JOGGING, STAIR_CLIMBING
}
enum class MealType{
    BREAKFAST, LUNCH, DINNER
}
data class ResponseUser(val user:User?)
data class ResponseUserLog(val userLog:UserLog?)
data class Foods(val foods: ArrayList<Food>)
data class Message(val message: String)

data class User(var uid: String?="",
                @SerializedName("body_form")
                var bodyForm:String?="",
                var height:Double?=0.0,
                var name: String?="",
                var password:String?="",
                var weight:Double?=0.0)
data class UserLog(val uid: String?,
                   val dates:ArrayList<DateLog>?)

data class DateLog(val date: String?,
                val exercises:ArrayList<Exercise>?,
                val meals:ArrayList<Meal>?,
                @SerializedName("diet_info")
                   var dietInfo: DietInfo?,
                   @SerializedName("breakfast_image")
                   val breakfastImage:String?,
                   @SerializedName("lunch_image")
                   val lunchImage:String?,
                   @SerializedName("dinner_image")
                   val dinnerImage:String?
)
data class Exercise(
    val exercise: String? = "",
    val reps: Int? = 1,
    val time: Int? = 0,
    val burned_kcal: Double? = 0.0
)
data class Food(
    val name: String? = "",
    val kcal: Double? = 0.0,
    val carbs: Double? = 0.0,
    val sugar: Double? = 0.0,
    val fat: Double? = 0.0,
    val protein: Double? = 0.0,
    val salt: Double? = 0.0
)
data class Meal(
    val food: String? = "",
    val size: String? = "",
    val kind: String? = "",
    val kcal: Double? = 0.0,
    val carbs: Double? = 0.0,
    val protein: Double? = 0.0,
    val fat: Double? = 0.0
)
data class PostMeal(
    val food: String? = "",
    val gram: Int? = 1,
    val kind: String? = "",
)

data class PostImage(
    val image: String? = "",
    val kind: String? = "",
)
data class DietInfo(
    @SerializedName("intake_kcal")
    val intakeKcal: Double? = 0.0,
    @SerializedName("burned_kcal")
    val burnedKcal: Double? = 0.0,
    @SerializedName("exercise_time")
    val exerciseTime: Int? = 0,
    val weight: Double? = 0.0,
    @SerializedName("intake_carbs")
    val intakeCarbs: Double? = 0.0,
    @SerializedName("intake_protein")
    val intakeProtein: Double? = 0.0,
    @SerializedName("intake_fat")
    val intakeFat: Double? = 0.0
)
data class TodayInfo(
    var breakfastKcal: Double = 0.0,
    var breakfastCarbohydrate: Double = 0.0,
    var breakfastProtein: Double = 0.0,
    var breakfastFat: Double = 0.0,
    var lunchKcal: Double = 0.0,
    var lunchCarbohydrate: Double = 0.0,
    var lunchProtein: Double = 0.0,
    var lunchFat: Double = 0.0,
    var dinnerKcal: Double = 0.0,
    var dinnerCarbohydrate: Double = 0.0,
    var dinnerProtein: Double = 0.0,
    var dinnerFat: Double = 0.0
)

interface RestAPI {
    @GET("userLogs/{uid}")
    fun getUserLog(@Path("uid") uid:String):Call<ResponseUserLog>

    @GET("foods")
    fun getFoods():Call<Foods>

    @GET("users/{uid}")
    fun getUser(@Path("uid") uid:String):Call<ResponseUser>

    @POST("userLogs/{uid}/{date}/meals")
    fun postMeal(@Path("uid") uid:String, @Path("date") date:String, @Body payload:PostMeal):Call<Message>

    @POST("userLogs/{uid}/{date}/exercises")
    fun postExercise(@Path("uid") uid:String, @Path("date") date:String, @Body payload:Exercise):Call<Message>

    @POST("foods")
    fun postFood(@Body payload:Food):Call<Message>

    @POST("userLogs/{uid}/{date}/image")
    fun postImage(@Path("uid") uid:String, @Path("date") date:String, @Body payload:PostImage):Call<Message>
}
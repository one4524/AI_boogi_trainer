package com.example.boogi_trainer.repository

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.round


class APIManager {
    companion object{
        lateinit var user:User
        lateinit var userLog:UserLog
        lateinit var todayLog:DateLog
        var todayInfo:TodayInfo = TodayInfo()

        private val caller = RetrofitClient.restAPI

        @RequiresApi(Build.VERSION_CODES.O)
        val now: LocalDate = LocalDate.now()
        @RequiresApi(Build.VERSION_CODES.O)
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")!!
        @RequiresApi(Build.VERSION_CODES.O)
        val today: String = now.format(formatter)

        private fun setUser(uid:String) {
            user= caller.getUser(uid).execute().body()?.user!!
        }

        private fun setUserLog(uid:String){
            userLog = caller.getUserLog(uid).execute().body()?.userLog!!
            setTodayLog()
            setTodayInfo()
        }
        fun getUser(uid:String): User {
            todayInfo = TodayInfo()
            setUser(uid)
            setUserLog(uid)
            return user
        }

        private fun setTodayLog() { // 금일 데이터 초기화
            var i = 0
            if(userLog.dates?.size == 0){ // 로그 데이터 자체가 없는 경우
                todayLog = DateLog(date = today, exercises = null, meals = null, dietInfo = DietInfo())
            }
            for(date in userLog.dates!!){
                if(date.date == today) {
                    todayLog = date
                    if(todayLog.meals?.size==0){ // 오늘 먹은게 없는 경우
                        todayLog.meals!!.add(Meal())
                    }
                    if(todayLog.exercises?.size==0){ // 오늘 운동한게 없는 경우
                        todayLog.exercises!!.add(Exercise())
                    }
                    break
                } else{ // 금일 데이터 자체가 없는 경우
                    todayLog = DateLog(date = today, exercises = null, meals = null, dietInfo = DietInfo())
                }
                i+=1
            }
        }

        private fun setTodayInfo(){
            if(todayLog.meals!=null){
                for(meal in todayLog.meals!!){
                    when (meal.kind) {
                        "breakfast" -> {
                            todayInfo.breakfastKcal += round(meal.kcal!!)
                            todayInfo.breakfastCarbohydrate += round(meal.carbs!!)
                            todayInfo.breakfastProtein += round(meal.protein!!)
                            todayInfo.breakfastFat += round(meal.fat!!)
                        }
                        "lunch" -> {
                            todayInfo.lunchKcal += round(meal.kcal!!)
                            todayInfo.lunchCarbohydrate += round(meal.carbs!!)
                            todayInfo.lunchProtein += round(meal.protein!!)
                            todayInfo.lunchFat += round(meal.fat!!)
                        }
                        "dinner" -> {
                            todayInfo.dinnerKcal += round(meal.kcal!!)
                            todayInfo.dinnerCarbohydrate += round(meal.carbs!!)
                            todayInfo.dinnerProtein += round(meal.protein!!)
                            todayInfo.dinnerFat += round(meal.fat!!)
                        }
                        "" ->{}
                    }
                    println(todayInfo)
                }
            }
        }
        //POST
        fun postMeal(food:String, size:String, kind:String, date:String= today){
            var payload = PostMeal(food, size, kind)
            if(caller.postMeal(user.uid!!,date,payload).execute().isSuccessful)
                getUser(user.uid!!)
        }
        fun postExercise(exercise:String, reps:Int, date:String= today){
            var payload = Exercise(exercise, reps)
            if(caller.postExercise(user.uid!!,date,payload).execute().isSuccessful)
                getUser(user.uid!!)
        }
    }
}
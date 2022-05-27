package com.example.boogi_trainer.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.round


class APIManager {
    companion object{
        lateinit var user:User
        lateinit var userLog:UserLog
        lateinit var todayLog:DateLog
        lateinit var foods:ArrayList<Food>
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

        fun setFoods() {
            foods = caller.getFoods().execute().body()?.foods!!
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
            setFoods()
            return user
        }

        fun getFood(foodName:String) : Food {
            var mfood:Food = Food()
            for(food in foods){
                if(food.name == foodName)
                    mfood = food
            }
            return mfood!!
        }

        fun getFoodName(foodName:String) : String {
            var mfood:Food = Food()
            for(food in foods){
                if(food.name == foodName)
                    mfood = food
            }
            return mfood.name!!
        }

        fun getFoodKcal(foodName:String, gram: Int) : Double {
            var mfood:Food = Food()
            for(food in foods){
                if(food.name == foodName)
                    mfood = food
            }
            return round(mfood.kcal!! * gram)
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
                    if(todayLog.dietInfo == null){
                        todayLog.dietInfo = DietInfo();
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
                }
            }
        }
        //POST
        fun postMeal(food:String, gram:Int, mealType: MealType, image:Bitmap, date:String= today){
            val image = bitmapToString(image)
            var kind = ""
            kind = when(mealType){
                MealType.BREAKFAST -> "breakfast"
                MealType.LUNCH -> "lunch"
                MealType.DINNER -> "dinner"
            }
            var payload = PostMeal(food, gram, kind, image)
            println(payload.toString())
            println(payload)
            if(caller.postMeal(user.uid!!,date,payload).execute().isSuccessful)
                getUser(user.uid!!)
        }
        fun postExercise(exerciseType:ExerciseType, reps:Int, date:String= today){
            var exercise = ""
            exercise = when(exerciseType){
                ExerciseType.PUSH_UP -> "푸쉬업"
                ExerciseType.SQUAT -> "스쿼트"
                ExerciseType.PULL_UP -> "풀업"
                ExerciseType.SIT_UP -> "윗몸일으키기"
                ExerciseType.DEAD_LIFT -> "데드리프트"
                ExerciseType.BARBELL_ROW -> "바벨로우"
                ExerciseType.BARBELL_CURL -> "바벨컬"
                ExerciseType.DUMBBELL_CURL -> "덤벨컬"
                ExerciseType.PLANK -> "플랭크"
            }
            var payload = Exercise(exercise, reps)
            if(caller.postExercise(user.uid!!,date,payload).execute().isSuccessful)
                getUser(user.uid!!)
        }
        fun postFood(food:Food){
            var payload = food
            caller.postFood(food).execute()
        }
        fun postCardioExercise(cardioExerciseType: CardioExerciseType, time:Int, date:String= today){
            var exercise = when(cardioExerciseType){
                CardioExerciseType.RUNNING_MACHINE -> "런닝머신"
                CardioExerciseType.JOGGING -> "조깅"
                CardioExerciseType.STAIR_CLIMBING -> "계단오르기"
            }
            var payload = Exercise(exercise, time = time)
            if(caller.postExercise(user.uid!!,date,payload).execute().isSuccessful){
                getUser(user.uid!!)
            }
        }

        private fun bitmapToString(bitmap: Bitmap): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }
        fun stringToBitmap(encodedString: String): Bitmap {
            val encodeByte = Base64.decode(encodedString, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        }
    }
}
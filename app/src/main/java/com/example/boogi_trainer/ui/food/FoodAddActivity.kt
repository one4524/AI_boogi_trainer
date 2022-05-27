package com.example.boogi_trainer.ui.food

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.boogi_trainer.databinding.ActivityFoodAddBinding
import com.example.boogi_trainer.repository.APIManager
import com.example.boogi_trainer.repository.Food
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FoodAddActivity: AppCompatActivity() {
    private lateinit var binding: ActivityFoodAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.completeButton.setOnClickListener {
            val name = binding.editTextFood.text.toString()
            // 용량이 입력되었을 경우에만 저장
            val gram = binding.editTextUnit.text.toString().toDouble()
            if (gram > 0) {
                val kcal = binding.editTextKcal.text.toString().toInt() / gram
                val carbs = binding.editTextCarbohydrate.text.toString().toInt() / gram
                val prot = binding.editTextProtein.text.toString().toInt() / gram
                val fat = binding.editTextFat.text.toString().toInt() / gram
                val food = Food(name = name,
                    kcal = kcal,
                    carbs = carbs,
                    protein = prot,
                    fat = fat)
                runBlocking {
                    GlobalScope.launch {
                        APIManager.postFood(food)
                    }
                }
                Toast.makeText(this, "저장 되었습니다.", Toast.LENGTH_SHORT).show()
                val i = Intent(this, FoodSearchActivity::class.java)
                startActivity(i)
                finish()
            }
            else {
                Toast.makeText(this, "용량을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
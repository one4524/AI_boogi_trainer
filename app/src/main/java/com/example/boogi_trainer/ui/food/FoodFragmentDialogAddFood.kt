package com.example.boogi_trainer.ui.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.boogi_trainer.R
import com.example.boogi_trainer.databinding.FragmentFoodDialogAddFoodBinding
import com.example.boogi_trainer.repository.APIManager
import com.example.boogi_trainer.repository.Food
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FoodFragmentDialogAddFood : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //false로 설정해 주면 화면밖 혹은 뒤로가기 버튼시 다이얼로그가 dismiss 되지 않는다.
        isCancelable = true
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialog)
    }

    private lateinit var binding: FragmentFoodDialogAddFoodBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFoodDialogAddFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.completeButton.setOnClickListener {
            val name = binding.editTextFood.text.toString()
            val gram = binding.editTextUnit.text.toString().toDouble()
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
        }
    }
}
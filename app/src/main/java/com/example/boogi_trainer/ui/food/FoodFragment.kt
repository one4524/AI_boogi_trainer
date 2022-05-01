package com.example.boogi_trainer.ui.food

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.boogi_trainer.MainActivity
import com.example.boogi_trainer.databinding.FragmentFoodBinding

class FoodFragment : Fragment() {

    private val breakfast = "아침"
    private val launch = "점심"
    private val dinner = "저녁"

    private var _binding: FragmentFoodBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(FoodViewModel::class.java)

        _binding = FragmentFoodBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 프로그레스바로 텍스트 뷰에 있는 숫자를 가져와서 표시함
        // 총 섭취량 바
        val myTotalKcal = Integer.parseInt(binding.myTotalKcal.text.toString())
        val goalTotalKcal = Integer.parseInt(binding.goalTotalKcal.text.toString())
        val totalProgress = (myTotalKcal.toDouble()/goalTotalKcal.toDouble()*100).toInt()
        binding.totalKcalBar.setProgress(totalProgress)
        // 단백질 바
        val myProtein = Integer.parseInt(binding.myProtein.text.toString())
        val goalProtein = Integer.parseInt(binding.goalProtein.text.toString())
        val proteinProgress = (myProtein.toDouble()/goalProtein.toDouble()*100).toInt()
        binding.proteinBar.setProgress(proteinProgress)
        // 탄수화물 바
        val myCarbohydrate = Integer.parseInt(binding.myCarbohydrate.text.toString())
        val goalCarbohydrate = Integer.parseInt(binding.goalCarbohydrate.text.toString())
        val carbohydrateProgress = (myCarbohydrate.toDouble()/goalCarbohydrate.toDouble()*100).toInt()
        binding.carbohydrateBar.setProgress(carbohydrateProgress)
        // 지방 바
        val myFat = Integer.parseInt(binding.myFat.text.toString())
        val goalFat = Integer.parseInt(binding.goalFat.text.toString())
        val fatProgress = (myFat.toDouble()/goalFat.toDouble()*100).toInt()
        binding.fatBar.setProgress(fatProgress)


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 식단 이미지 부분을 클릭했을 때 사진 촬영
        binding.breakfastImage.setOnClickListener {
            //FoodFragmentDialog().show(childFragmentManager, "dialog is working")
            startFoodCamera(breakfast)
        }
        binding.lunchImage.setOnClickListener {
            startFoodCamera(launch)
        }
        binding.dinnerImage.setOnClickListener {
            startFoodCamera(dinner)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startFoodCamera(mealTime: String) {
        val intent = Intent (getActivity(), FoodCameraActivity::class.java)
        intent.putExtra("mealTime", mealTime)
        getActivity()?.startActivity(intent)
    }
}
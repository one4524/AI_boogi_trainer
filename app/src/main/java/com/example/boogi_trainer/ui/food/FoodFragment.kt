package com.example.boogi_trainer.ui.food

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.boogi_trainer.MainActivity
import com.example.boogi_trainer.databinding.FragmentFoodBinding
import com.example.boogi_trainer.repository.APIManager
import com.example.boogi_trainer.repository.DateLog
import com.example.boogi_trainer.repository.DietInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FoodFragment : Fragment() {
    private val breakfast = "아침"
    private val lunch = "점심"
    private val dinner = "저녁"

    private var _binding: FragmentFoodBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // 유저 정보 가져오기
    var todayLog = APIManager.todayLog
    var todayInfo = APIManager.todayInfo

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

        val myTotalKcal = todayLog.dietInfo?.intakeKcal!!
        val goalTotalKcal = "2400"
        val totalProgress = (myTotalKcal/goalTotalKcal.toDouble()*100).toInt()
        binding.myTotalKcal.text = myTotalKcal.toString()
        binding.goalTotalKcal.text = goalTotalKcal
        binding.totalKcalBar.setProgress(totalProgress)
        // 단백질 바
        val myProtein = todayLog.dietInfo?.intakeProtein!!
        val goalProtein = "70"
        val proteinProgress = (myProtein/goalProtein.toDouble()*100).toInt()
        binding.myProtein.text = myProtein.toString()
        binding.goalProtein.text = goalProtein
        binding.proteinBar.setProgress(proteinProgress)
        // 탄수화물 바
        val myCarbohydrate = todayLog.dietInfo?.intakeCarbs!!
        val goalCarbohydrate = "500"
        val carbohydrateProgress = (myCarbohydrate/goalCarbohydrate.toDouble()*100).toInt()
        binding.myCarbohydrate.text = myCarbohydrate.toString()
        binding.goalCarbohydrate.text = goalCarbohydrate
        binding.carbohydrateBar.setProgress(carbohydrateProgress)
        // 지방 바
        val myFat = todayLog.dietInfo?.intakeFat!!
        val goalFat = "40"
        val fatProgress = (myFat/goalFat.toDouble()*100).toInt()
        binding.myFat.text = myFat.toString()
        binding.goalFat.text = goalFat
        binding.fatBar.setProgress(fatProgress)

        /* 식단기록 */
        binding.breakfastKcal.text = todayInfo.breakfastKcal.toString()
        binding.breakfastCarbohydrate.text = todayInfo.breakfastCarbohydrate.toString()
        binding.breakfastProtein.text = todayInfo.breakfastProtein.toString()
        binding.breakfastFat.text = todayInfo.breakfastFat.toString()


        binding.lunchKcal.text = todayInfo.lunchKcal.toString()
        binding.lunchCarbohydrate.text = todayInfo.lunchCarbohydrate.toString()
        binding.lunchProtein.text = todayInfo.lunchProtein.toString()
        binding.lunchFat.text = todayInfo.lunchFat.toString()


        binding.dinnerKcal.text = todayInfo.dinnerKcal.toString()
        binding.dinnerCarbohydrate.text = todayInfo.dinnerCarbohydrate.toString()
        binding.dinnerProtein.text = todayInfo.dinnerProtein.toString()
        binding.dinnerFat.text = todayInfo.dinnerFat.toString()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 식단 이미지 부분을 클릭했을 때 사진 촬영
        binding.breakfastImage.setOnClickListener {
            startFoodCamera(breakfast)
        }
        binding.lunchImage.setOnClickListener {
            startFoodCamera(lunch)
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
package com.example.boogi_trainer.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.boogi_trainer.FindFoodImage
import com.example.boogi_trainer.databinding.FragmentHomeBinding
import com.example.boogi_trainer.repository.APIManager
import com.example.boogi_trainer.repository.APIManager.Companion.todayLog
import com.example.boogi_trainer.ui.food.FoodDetailData
import kotlin.random.Random

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    // 리사이클러뷰가 불러올 목록
    private val dataCarb: MutableList<FoodDetailData> = mutableListOf()
    private val dataProt: MutableList<FoodDetailData> = mutableListOf()
    private val dataFat: MutableList<FoodDetailData> = mutableListOf()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // 유저 이름 표시
        _binding!!.userName.text = APIManager.user.name
        // 오늘 섭취한 칼로리 표시
        val foodKcal = APIManager.todayLog.dietInfo?.intakeKcal
        binding.foodKcal.text = foodKcal.toString()
        // 오늘 소비한 칼로리 표시
        val burnedKcal = APIManager.todayLog.dietInfo?.burnedKcal
        binding.exerciseKcal.text = burnedKcal.toString()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 리사이클러뷰 연결
        initialize() // 리사이클러뷰에 아이템 추가
        refreshRecyclerView()
        // 리사이클러뷰 연결

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initialize() {
        // 필요 단탄지 계산
        val myProtein = todayLog.dietInfo?.intakeProtein!!
        val myCarbohydrate = todayLog.dietInfo?.intakeCarbs!!
        val myFat = todayLog.dietInfo?.intakeFat!!
        val goalProtein = 70
        val goalCarbohydrate = 100
        val goalFat = 40
        val needProtein = goalProtein - myProtein
        val needCarbohydrate = goalCarbohydrate - myCarbohydrate
        val needFat = goalFat - myFat
        // 필요 단탄지 계산

        // 필요 단탄지 표시
        binding.textCarbohydrate.text = needCarbohydrate.toInt().toString()
        binding.textProtein.text = needProtein.toInt().toString()
        binding.textFat.text = needFat.toInt().toString()
        // 필요 단탄지 표시

        val random = Random

        // 탄수화물
        for (i in 1..4) { // 4개 음식 추천
            // 랜덤으로 음식 뽑아
            val foodName = FindFoodImage().foodImage.entries.elementAt(random.nextInt(FindFoodImage().foodImage.size)).key
            // 몇 그램 필요한지 계산
            val foodGram = needCarbohydrate / APIManager.getFood(foodName).carbs!!
            if (foodGram != null) {
                with(dataCarb) {
                    add(FoodDetailData(foodName, foodGram.toInt().toString()))
                }
            }
        }
        // 탄수화물

        // 단백질
        for (i in 1..4) { // 4개 음식 추천
            // 랜덤으로 음식 뽑아
            val foodName = FindFoodImage().foodImage.entries.elementAt(random.nextInt(FindFoodImage().foodImage.size)).key
            // 몇 그램 필요한지 계산
            val foodGram = needProtein / APIManager.getFood(foodName).protein!!
            if (foodGram != null) {
                with(dataProt) {
                    add(FoodDetailData(foodName, foodGram.toInt().toString()))
                }
            }
        }
        // 단백질

        // 지방
        for (i in 1..4) { // 4개 음식 추천
            // 랜덤으로 음식 뽑아
            val foodName = FindFoodImage().foodImage.entries.elementAt(random.nextInt(FindFoodImage().foodImage.size)).key
            // 몇 그램 필요한지 계산
            val foodGram = needFat / APIManager.getFood(foodName).fat!!
            if (foodGram != null) {
                with(dataFat) {
                    add(FoodDetailData(foodName, foodGram.toInt().toString()))
                }
            }
        }
        // 지방
    }

    private fun refreshRecyclerView() {
        val adapterCarb = HomeAdapter()
        val adapterProt = HomeAdapter()
        val adapterFat = HomeAdapter()

        // 탄수화물 부분
        adapterCarb!!.foodData = dataCarb
        binding.recyclerViewCarbohydrate.adapter = adapterCarb
        binding.recyclerViewCarbohydrate.layoutManager = LinearLayoutManager(binding.root.context, RecyclerView.HORIZONTAL, false)

        // 단백질 부분
        adapterProt!!.foodData = dataProt
        binding.recyclerViewProtein.adapter = adapterProt
        binding.recyclerViewProtein.layoutManager = LinearLayoutManager(binding.root.context, RecyclerView.HORIZONTAL, false)

        // 지방 부분
        adapterFat!!.foodData = dataFat
        binding.recyclerViewFat.adapter = adapterFat
        binding.recyclerViewFat.layoutManager = LinearLayoutManager(binding.root.context, RecyclerView.HORIZONTAL, false)
    }

}
package com.example.boogi_trainer.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.boogi_trainer.databinding.FragmentHomeBinding
import com.example.boogi_trainer.repository.APIManager
import com.example.boogi_trainer.ui.food.FoodDetailAdapter
import com.example.boogi_trainer.ui.food.FoodDetailData

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

        _binding!!.userName.text = APIManager.user.name

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
        with(dataCarb) {
            add(FoodDetailData("food1", "100"))
            add(FoodDetailData("food2", "100"))
            add(FoodDetailData("food3", "100"))
            add(FoodDetailData("food3", "100"))
            add(FoodDetailData("food3", "100"))
        }
        with(dataProt) {
            add(FoodDetailData("food4", "100"))
            add(FoodDetailData("food5", "100"))
            add(FoodDetailData("food6", "100"))
            add(FoodDetailData("food6", "100"))
            add(FoodDetailData("food6", "100"))
        }
        with(dataFat) {
            add(FoodDetailData("food7", "100"))
            add(FoodDetailData("food8", "100"))
            add(FoodDetailData("food9", "100"))
            add(FoodDetailData("food9", "100"))
            add(FoodDetailData("food9", "100"))
        }
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
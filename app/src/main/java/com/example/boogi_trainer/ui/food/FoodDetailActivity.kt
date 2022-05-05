package com.example.boogi_trainer.ui.food

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.boogi_trainer.databinding.ActivityFoodDetailBinding
import com.google.android.material.tabs.TabLayoutMediator

class FoodDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoodDetailBinding
    private val tabTitleArray = arrayOf(
        "간편 입력",
        "상세 입력"
    )
    // 리사이클러뷰가 불러올 목록
    private val data: MutableList<FoodDetailData> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFoodDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // FoodCameraActivity에서 넘어온 Intent 받음
        val intent = getIntent()
        val uri = intent.getParcelableExtra<Uri>("imageUri")
        val mealTime = intent.getStringExtra("mealTime")


        binding.imageView.setImageURI(uri) // imageView에 가져온 이미지 삽입
        binding.textMealTime.text = mealTime // 아침 점심 저녁 표시
        binding.buttonAddFood.setOnClickListener { // 음식 추가 버튼을 누르면 음식 검색하는 다이얼로그 나옴
            FoodFragmentDialogSearch().show(supportFragmentManager, "dialog is working")
        }
        binding.buttonFoodSearch.setOnClickListener { // 음식 검색 버튼을 누르면 음식 검색하는 다이얼로그 나옴
            FoodFragmentDialogSearch().show(supportFragmentManager, "dialog is working")
        }

        // 탭 레이아웃 연결
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        viewPager.adapter = FoodViewPagerAdapter(supportFragmentManager, lifecycle)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitleArray[position]
        }.attach()
        // 탭 레이아웃 연결

        // 리사이클러뷰 연결
        initialize() // 리사이클러뷰에 아이템 추가
        refreshRecyclerView()
        // 리사이클러뷰 연결


    }

    private fun initialize() {
        with(data) {
            add(FoodDetailData("food1"))
            add(FoodDetailData("food2"))
            add(FoodDetailData("food3"))
        }
    }

    private fun refreshRecyclerView() {
        val adapter = FoodDetailAdapter()
        adapter!!.foodData = data
        binding.rvFoodList.adapter = adapter
        binding.rvFoodList.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
    }
}
package com.example.boogi_trainer.ui.food

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.get
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.boogi_trainer.databinding.ActivityFoodDetailBinding
import com.example.boogi_trainer.databinding.RecyclerviewFoodListItemBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_food_detail.*
import kotlinx.android.synthetic.main.recyclerview_food_list_item.view.*

class FoodDetailActivity : AppCompatActivity(){

    private lateinit var binding: ActivityFoodDetailBinding

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

        // 리사이클러뷰 연결
        initialize() // 리사이클러뷰에 아이템 추가
        refreshRecyclerView()
        // 리사이클러뷰 연결


        // 리사이클러뷰에서 데이터 가져와 서버로 보내야됨
        binding.buttonComplete.setOnClickListener {
            val rvFoodList = binding.rvFoodList
            for (i in data.indices) {
                data[i].name = rvFoodList[i].listItemFoodName.text.toString()
                data[i].gram = rvFoodList[i].editTextGram.text.toString()
            }
            println("----------$data")
        }
        // 리사이클러뷰에서 데이터 가져와 서버로 보내야됨
    }

    private fun initialize() {
        with(data) {
            add(FoodDetailData("food1", "100"))
            add(FoodDetailData("food2", "100"))
            add(FoodDetailData("food3", "100"))
        }
    }

    private fun refreshRecyclerView() {
        val adapter = FoodDetailAdapter()
        adapter!!.foodData = data
        binding.rvFoodList.adapter = adapter
        binding.rvFoodList.layoutManager = LinearLayoutManager(this)
    }

}
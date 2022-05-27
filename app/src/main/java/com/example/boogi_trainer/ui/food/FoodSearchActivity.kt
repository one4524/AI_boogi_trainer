package com.example.boogi_trainer.ui.food

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.boogi_trainer.databinding.ActivityFoodSearchBinding
import com.example.boogi_trainer.repository.APIManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FoodSearchActivity : AppCompatActivity() {

    private lateinit var rv_foodName: RecyclerView
    private lateinit var adapter: FoodSearchAdapter
    private var foodNameList = ArrayList<String>()

    private lateinit var searchFoodName: SearchView
    private lateinit var binding: ActivityFoodSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rv_foodName = binding.rvFoodName // recyclerView
        searchFoodName = binding.searchFoodName // searchView
        searchFoodName.setOnQueryTextListener(searchViewTextListener)

        initialization()
        setAdapter()

        // 카메라 액티비티에서 넘어온 인텐트
        val intent = getIntent()
        adapter.mealTime = intent.getStringExtra("mealTime").toString()

        binding.floatingActionButton.setOnClickListener {
            val i = Intent(this, FoodAddActivity::class.java)
            finish()
            startActivity(i)
        }
    }

    //SearchView 텍스트 입력시 이벤트
    private var searchViewTextListener: SearchView.OnQueryTextListener =
        object : SearchView.OnQueryTextListener {
            //검색버튼 입력시 호출, 검색버튼이 없으므로 사용하지 않음
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            //텍스트 입력/수정시에 호출
            override fun onQueryTextChange(s: String): Boolean {
                adapter.filter.filter(s)
                return false
            }
        }

    private fun setAdapter() {
        //리사이클러뷰에 리사이클러뷰 어댑터 부착
        rv_foodName.layoutManager = LinearLayoutManager(this)
        adapter = FoodSearchAdapter(foodNameList, this)
        rv_foodName.adapter = adapter
    }

    private fun initialization() {
        runBlocking {
            GlobalScope.launch {
                foodNameList.clear()
                APIManager.setFoods()
                for (food in APIManager.foods) {
                    with(foodNameList) {
                        add(food.name!!)
                    }
                }

            }
        }
    }
}
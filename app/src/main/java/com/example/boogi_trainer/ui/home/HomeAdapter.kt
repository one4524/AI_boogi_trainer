package com.example.boogi_trainer.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.boogi_trainer.databinding.RecyclerviewHomeItemBinding
import com.example.boogi_trainer.ui.food.FoodDetailData


class HomeAdapter: RecyclerView.Adapter<HomeHolder>() {
    var foodData = mutableListOf<FoodDetailData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeHolder {
        val binding = RecyclerviewHomeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeHolder, position: Int) {
        val foodDetailData = foodData[position]
        holder.setData(foodDetailData, position)
    }

    override fun getItemCount(): Int {
        return foodData.size
    }
}

class HomeHolder(val binding: RecyclerviewHomeItemBinding): RecyclerView.ViewHolder(binding.root) {

    fun setData(foodDetailData: FoodDetailData, position: Int) {
        binding.homeItemFoodName.text = foodDetailData.name
        binding.homeItemFoodGram.text = "${foodDetailData.gram}g"
    }
}
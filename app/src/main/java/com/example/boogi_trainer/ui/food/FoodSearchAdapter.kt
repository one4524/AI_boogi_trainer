package com.example.boogi_trainer.ui.food

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.boogi_trainer.R
import com.example.boogi_trainer.databinding.RecyclerviewFoodSearchItemBinding

class FoodSearchAdapter: RecyclerView.Adapter<Holder>() {

    var foodData = mutableListOf<FoodSearchData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = RecyclerviewFoodSearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val foodSearchData = foodData[position]
        holder.setData(foodSearchData)
    }

    override fun getItemCount(): Int = foodData.size


}

class Holder(val binding: RecyclerviewFoodSearchItemBinding): RecyclerView.ViewHolder(binding.root) {
    fun setData(foodSearchData: FoodSearchData) {
        binding.rvTextFoodName.text = foodSearchData.foodName
    }

}
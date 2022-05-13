package com.example.boogi_trainer.ui.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.boogi_trainer.databinding.RecyclerviewFoodListItemBinding

class FoodDetailAdapter: RecyclerView.Adapter<DetailHolder>() {
    var foodData = mutableListOf<FoodDetailData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailHolder {
        val binding = RecyclerviewFoodListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailHolder, position: Int) {
        val foodDetailData = foodData[position]
        holder.setData(foodDetailData, position)
    }

    override fun getItemCount(): Int {
        return foodData.size
    }
}

class DetailHolder(val binding: RecyclerviewFoodListItemBinding): RecyclerView.ViewHolder(binding.root) {
    fun setData(foodDetailData: FoodDetailData, position: Int) {
        binding.listItemFoodName.text = foodDetailData.name
        binding.editTextGram.setText(foodDetailData.gram)
    }
}
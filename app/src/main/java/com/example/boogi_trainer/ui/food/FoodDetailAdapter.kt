package com.example.boogi_trainer.ui.food

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.boogi_trainer.FindFoodImage
import com.example.boogi_trainer.MainActivity
import com.example.boogi_trainer.databinding.RecyclerviewFoodListItemBinding
import com.example.boogi_trainer.repository.APIManager

class FoodDetailAdapter(): RecyclerView.Adapter<DetailHolder>() {
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

    init {
        // 음식 검색 버튼을 누르면 높이 0으로 숨어있던 레이아웃의 높이를 변경해서 음식을 검색할 수 있는 editText와 버튼이 나온다
        binding.buttonFoodSearch.setOnClickListener {
            changeHeightLayoutFoodSearch(150)
        }
        // 확인 버튼을 누르면 editText에 임력된 음식 이름이 서버에 있는지 확인하고
        // 있으면 listItemFoodName을 변경하고 높이를 0으로 만들어 레이아웃 숨김
        binding.buttonCheck.setOnClickListener {
            val name = binding.editTextFoodName.text.toString()
            if (APIManager.getFood(name).name != "") {
                binding.listItemFoodName.text = name
                changeHeightLayoutFoodSearch(0)
            }
            else {
                Toast.makeText(binding.root.context, "음식이 존재하지 않습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setData(foodDetailData: FoodDetailData, position: Int) {
        binding.listItemFoodName.text = foodDetailData.name
        binding.editTextGram.setText(foodDetailData.gram)
        try {
            val res = MainActivity.context().resources.getIdentifier(
                "${FindFoodImage().foodImage[foodDetailData.name]}",
                "drawable",
                MainActivity.context().packageName)
            binding.listItemFoodImage.setImageResource(res)
        } catch (e: NullPointerException) {}
    }

    private fun changeHeightLayoutFoodSearch(height: Int) {
        val layoutParams = binding.layoutFoodSearch.layoutParams
        layoutParams.height = height
        binding.layoutFoodSearch.layoutParams = layoutParams
    }
}
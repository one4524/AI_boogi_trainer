package com.example.boogi_trainer.ui.food

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.boogi_trainer.R

class FoodSearchAdapter(var foods: ArrayList<String>, var mContext: Context): RecyclerView.Adapter<FoodSearchAdapter.Holder>(), Filterable {

    var foodNameList = ArrayList<String>()
    var itemFilter = ItemFilter()
    var mealTime = ""

    init {
        foodNameList.addAll(foods)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.recyclerview_food_search_item, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val foodSearchData = foodNameList[position]
        holder.food.text = foodSearchData
    }

    override fun getItemCount(): Int = foodNameList.size

    override fun getFilter(): Filter {
        return itemFilter
    }

    inner class Holder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var food: TextView
        init {
            food = itemView.findViewById(R.id.rv_textFoodName)

            itemView.setOnClickListener {
                val intent = Intent(mContext, FoodDetailActivity::class.java)
                intent.putExtra("foodName", food.text)
                intent.putExtra("mealTime", mealTime)
                mContext.startActivity(intent)
                val activity: FoodSearchActivity = mContext as FoodSearchActivity
                activity.finish()
            }
        }
    }

    inner class ItemFilter : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val filterString = charSequence.toString()
            val results = FilterResults()

            // 검색 없을 경우 원본 배열
            val filteredList: ArrayList<String> = ArrayList<String>()
            // 검색어가 들어오면
            if (filterString.trim { it <= ' ' }.isNotEmpty()) {
                for (food in foods) {
                    if (food.contains(filterString)) {
                        filteredList.add(food)
                    }
                }
            }
            results.values = filteredList
            results.count = filteredList.size

            return results
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
            foodNameList.clear()
            foodNameList.addAll(filterResults.values as ArrayList<String>)
            notifyDataSetChanged()
        }
    }
}


package com.example.boogi_trainer.ui.food

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.boogi_trainer.R
import com.example.boogi_trainer.databinding.FragmentFoodTab1Binding

class FoodFragmentTab1 : Fragment() {
    private lateinit var binding: FragmentFoodTab1Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFoodTab1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btSmall = binding.buttonSmall
        val btMedium = binding.buttonMedium
        val btLarge = binding.buttonlarge

        // 버튼 클릭 효과(테두리) 하나에만 적용되게 만들기
        btSmall.setOnClickListener {
            btSmall.setBackgroundResource(R.drawable.bt_highlight)
            btMedium.setBackgroundResource(R.color.white)
            btLarge.setBackgroundResource(R.color.white)
            dataPassListener.onDataPass(50) // FoodDetailActivity로 데이터 전달
        }
        btMedium.setOnClickListener {
            btMedium.setBackgroundResource(R.drawable.bt_highlight)
            btSmall.setBackgroundResource(R.color.white)
            btLarge.setBackgroundResource(R.color.white)
            dataPassListener.onDataPass(100) // FoodDetailActivity로 데이터 전달
        }
        btLarge.setOnClickListener {
            btLarge.setBackgroundResource(R.drawable.bt_highlight)
            btMedium.setBackgroundResource(R.color.white)
            btSmall.setBackgroundResource(R.color.white)
            dataPassListener.onDataPass(150) // FoodDetailActivity로 데이터 전달
        }
    }


    // FoodDetailActivity로 데이터 전달 위한 부분
    interface onDataPassListener {
        fun onDataPass(data: Int)
    }
    lateinit var dataPassListener : onDataPassListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPassListener = context as onDataPassListener //형변환
    }
    // FoodDetailActivity로 데이터 전달 위한 부분
}
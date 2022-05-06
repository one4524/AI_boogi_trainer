package com.example.boogi_trainer.ui.food

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.boogi_trainer.databinding.FragmentFoodTab2Binding
import java.lang.Integer.parseInt

class FoodFragmentTab2 : Fragment() {
    private lateinit var binding: FragmentFoodTab2Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFoodTab2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gram = binding.textFoodGram
        val kcal = binding.textFoodKcal

        gram.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val igram = gram.text.toString().toIntOrNull() ?: 0 // edittext의 text가 int가 안 되면 0으로 만듦
                val ikcal = kcal.text.toString().toIntOrNull() ?: 0 // edittext가 text가 int가 안 되면 0으로 만듦
                dataPassListener.onDataPass(igram, ikcal) // FoodDetailActivity로 데이터 전달
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        kcal.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val igram = gram.text.toString().toIntOrNull() ?: 0 // edittext가 text가 int가 안 되면 0으로 만듦
                val ikcal = kcal.text.toString().toIntOrNull() ?: 0 // edittext가 text가 int가 안 되면 0으로 만듦
                dataPassListener.onDataPass(igram, ikcal) // FoodDetailActivity로 데이터 전달
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }


    // FoodDetailActivity로 데이터 전달 위한 부분
    interface onDataPassListener {
        fun onDataPass(data1: Int, data2: Int)
    }
    lateinit var dataPassListener : onDataPassListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPassListener = context as onDataPassListener //형변환
    }
    // FoodDetailActivity로 데이터 전달 위한 부분
}
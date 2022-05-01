package com.example.boogi_trainer.ui.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.boogi_trainer.databinding.FragmentFoodTab2Binding

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
}
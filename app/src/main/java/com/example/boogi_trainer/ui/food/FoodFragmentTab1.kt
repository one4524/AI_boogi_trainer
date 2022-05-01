package com.example.boogi_trainer.ui.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
}
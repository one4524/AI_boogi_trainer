package com.example.boogi_trainer.ui.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.boogi_trainer.databinding.FragmentFoodDialogBinding

class FoodFragmentDialog : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //false로 설정해 주면 화면밖 혹은 뒤로가기 버튼시 다이얼로그가 dismiss 되지 않는다.
        isCancelable = true
    }

    private lateinit var binding: FragmentFoodDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFoodDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val text = "Hello asdf"

        binding.takePhoto.setOnClickListener {
            Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
        }
        binding.choosePhoto.setOnClickListener {
            Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
        }
        binding.searchFood.setOnClickListener {
            //Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
            FoodFragmentDialogSearch().show(childFragmentManager, "dialog is working")
        }
    }
}
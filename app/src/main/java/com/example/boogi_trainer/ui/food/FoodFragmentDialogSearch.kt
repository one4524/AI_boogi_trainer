package com.example.boogi_trainer.ui.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.boogi_trainer.R
import com.example.boogi_trainer.databinding.FragmentFoodDialogSearchBinding

class FoodFragmentDialogSearch : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //false로 설정해 주면 화면밖 혹은 뒤로가기 버튼시 다이얼로그가 dismiss 되지 않는다.
        isCancelable = true
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialog)
    }
    

    private lateinit var binding: FragmentFoodDialogSearchBinding
    // 리사이클러뷰가 가져올 음식 데이터 목록
    private var adapter: FoodSearchAdapter? = null
    private val data:MutableList<FoodSearchData> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFoodDialogSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.floatingActionButton.setOnClickListener {
            FoodFragmentDialogAddFood().show(childFragmentManager, "dialog is working")
        }

        initialize()// 음식 검색 목록
        refreshRecyclerView()

    }

    private fun initialize() {
        with(data) {
            add(FoodSearchData("food1"))
            add(FoodSearchData("food2"))
            add(FoodSearchData("food3"))
        }
    }
    private fun refreshRecyclerView() {
        adapter = FoodSearchAdapter()
        adapter!!.foodData = data
        binding.rvFoodName.adapter = adapter
        binding.rvFoodName.layoutManager = LinearLayoutManager(requireContext())
    }
}
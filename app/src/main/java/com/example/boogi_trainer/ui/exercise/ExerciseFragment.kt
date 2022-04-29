package com.example.boogi_trainer.ui.exercise

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.boogi_trainer.PoseActivity
import com.example.boogi_trainer.R
import com.example.boogi_trainer.databinding.FragmentExerciseBinding

class ExerciseFragment : Fragment() {

    private var _binding: FragmentExerciseBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(ExerciseViewModel::class.java)

        _binding = FragmentExerciseBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val btn : Button = binding.startExercise
        btn.setOnClickListener {
            val intent = Intent(context, PoseActivity::class.java)
            startActivity(intent)

        }

        val recyclerView : RecyclerView = binding.recycleView

        val list = ArrayList<exersiceData>()
        list.add(exersiceData(R.drawable.squat,"푸쉬업"))
        list.add(exersiceData(R.drawable.squat,"스탠딩니업"))
        list.add(exersiceData(R.drawable.squat,"스쿼트"))


        val adapter = context?.let { itemAdapter(it, list) }
        recyclerView.adapter = adapter


        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
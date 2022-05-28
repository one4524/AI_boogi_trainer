package com.example.boogi_trainer.ui.exercise

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.boogi_trainer.R
import com.example.boogi_trainer.RunningActivity
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
            ViewModelProvider(this)[ExerciseViewModel::class.java]

        _binding = FragmentExerciseBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val btn : ImageButton = binding.startExercise
        btn.setOnClickListener {
            val intent = Intent(context, RunningActivity::class.java)
            startActivity(intent)

        }

        binding.startBody.setOnClickListener {
            val intent = Intent(context, ExercisePartActivity::class.java)
            intent.putExtra("partName", 0)
            startActivity(intent)
        }
        binding.startUp.setOnClickListener {
            val intent = Intent(context, ExercisePartActivity::class.java)
            intent.putExtra("partName", 1)
            startActivity(intent)
        }
        binding.startDown.setOnClickListener {
            val intent = Intent(context, ExercisePartActivity::class.java)
            intent.putExtra("partName", 2)
            startActivity(intent)
        }
        binding.startArm.setOnClickListener {
            val intent = Intent(context, ExercisePartActivity::class.java)
            intent.putExtra("partName", 3)
            startActivity(intent)
        }

        val recyclerView : RecyclerView = binding.recycleView

        val list = ArrayList<exerciseData>()
        list.add(exerciseData(R.drawable.pushup_btn,"푸쉬업"))
        list.add(exerciseData(R.drawable.squat_btn,"스쿼트"))
        list.add(exerciseData(R.drawable.deadlift_btn,"데드리프트"))
        list.add(exerciseData(R.drawable.situp_btn,"싯업"))
        list.add(exerciseData(R.drawable.pullup_btn,"풀업"))
        list.add(exerciseData(R.drawable.babellrow_btn,"바벨로우"))
        list.add(exerciseData(R.drawable.dumbelcurl_btn,"덤벨컬"))
        list.add(exerciseData(R.drawable.babellcurl_btn,"바벨컬"))
        list.add(exerciseData(R.drawable.flank_btn,"플랭크"))

        val adapter = context?.let { itemAdapter(it, list) }
        recyclerView.adapter = adapter

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
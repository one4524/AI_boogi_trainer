package com.example.boogi_trainer.ui.exercise

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ExerciseViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is exercise Fragment"
    }
    val text: LiveData<String> = _text
}
package com.example.chucksgourmet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyViewModel : ViewModel() {
    private val _time = MutableLiveData<String>()
    val time: LiveData<String> get() = _time

    private val _date = MutableLiveData<String>()
    val date: LiveData<String> get() = _date

    fun setTime(value: String) {
        _time.value = value
    }

    fun setDate(value: String) {
        _date.value = value
    }
}
package com.example.newdiary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.haibin.calendarview.Calendar

class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val _liveDataMap = MutableLiveData<MutableMap<String, Calendar>>()
    val liveDataMap:LiveData<MutableMap<String, Calendar>> get() = _liveDataMap
    private val dataRepository = getApplication<MyApplication>().dataRepository

    init {
        _liveDataMap.value = dataRepository.calendarMap
    }

    fun add(calendar: Calendar){
        dataRepository.add(calendar)
        _liveDataMap.value = dataRepository.calendarMap
    }

    fun remove(calendar: Calendar){
        dataRepository.remove(calendar)
        _liveDataMap.value = dataRepository.calendarMap
    }

    fun saveData(){
        dataRepository.inSave()
    }
}
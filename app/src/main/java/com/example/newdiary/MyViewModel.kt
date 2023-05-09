package com.example.newdiary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MyViewModel(application: Application) : AndroidViewModel(application) {
    private var _liveCalendarMap = MutableLiveData<MutableMap<String,com.haibin.calendarview.Calendar>>()
    val liveCalendarMap:LiveData<MutableMap<String,com.haibin.calendarview.Calendar>> get() = _liveCalendarMap
    private val dataRepository = MyApplication.getInstance().dataRepository

    init {
        _liveCalendarMap = dataRepository.liveCalendarMap
    }

}
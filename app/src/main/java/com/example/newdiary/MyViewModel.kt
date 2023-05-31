package com.example.newdiary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.Calendar.Scheme

class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val _liveDataMap = MutableLiveData<MutableMap<String, Calendar>>()
    val liveDataMap: LiveData<MutableMap<String, Calendar>> get() = _liveDataMap
    private val dataRepository = getApplication<MyApplication>().dataRepository

    /**
     * 当前背景图像
     */
    var switchBackground = MutableLiveData(0)

    val warnNumber = dataRepository.warnNumbar

    init {
        _liveDataMap.value = dataRepository.calendarMap
    }

    fun add(calendar: Calendar) {
        dataRepository.add(calendar)
        _liveDataMap.value = dataRepository.calendarMap
    }

    fun remove(calendar: Calendar) {
        dataRepository.remove(calendar)
        _liveDataMap.value = dataRepository.calendarMap
    }

    fun importData() {
        dataRepository.importData()
        _liveDataMap.value = dataRepository.calendarMap
    }

    fun outData() {
        dataRepository.outData()
        _liveDataMap.value = dataRepository.calendarMap
    }

    fun clearAllData() {
        dataRepository.clearAllData()
        _liveDataMap.value = dataRepository.calendarMap
    }

    fun monthCount(year: Int, month: Int): Int {
        return dataRepository.monthCount(year, month)
    }

    fun allCount(): Int {
        return dataRepository.allCount()
    }

    fun oldDataToNewData() {
        dataRepository.oldDataToNewData()
        _liveDataMap.value = dataRepository.calendarMap
    }

    fun addScheme(calendar: Calendar, scheme: Scheme){
        dataRepository.addScheme(calendar, scheme)
        _liveDataMap.value = dataRepository.calendarMap
    }

    fun removeScheme(calendar: Calendar, scheme: Scheme){
        dataRepository.removeScheme(calendar, scheme)
        _liveDataMap.value = dataRepository.calendarMap
    }
}
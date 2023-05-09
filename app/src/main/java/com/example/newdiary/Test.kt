package com.example.newdiary

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView

class Test {
    companion object{
        @JvmStatic
        fun main(args: Array<String>) {
            val map = mutableMapOf<String,Calendar>()

            val calendar1 = Calendar()
            calendar1.year = 2023
            calendar1.month = 1
            calendar1.day = 10
            calendar1.scheme = "-1"
            println(calendar1.lunar)
            map[calendar1.toString()] = calendar1

            val calendar2 = Calendar()
            calendar2.day = calendar1.day - 2
            calendar2.scheme = "-2"
            map[calendar2.toString()] = calendar2

            println(map)

            val s = Gson().toJson(map)
            println(s)

            val map1:MutableMap<String,Calendar> = Gson().fromJson(s,object : TypeToken<MutableMap<String,Calendar>>(){}.type)
            println(map1)
        }
    }
}

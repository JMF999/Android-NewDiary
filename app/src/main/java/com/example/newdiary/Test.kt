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
            println("c1 lunar "+calendar1.lunar)
            println("c1: $calendar1")
            map[calendar1.toString()] = calendar1

            val calendar2 = Calendar()
            calendar2.day = calendar1.day - 2
            calendar2.scheme = "-2"
            map[calendar2.toString()] = calendar2

            println("map: $map")

            val s = Gson().toJson(map)
            println("json: $s")

            val map1:MutableMap<String,Calendar> = Gson().fromJson(s,object : TypeToken<MutableMap<String,Calendar>>(){}.type)
            val map2:Map<String,Calendar> = Gson().fromJson(s,object : TypeToken<Map<String,Calendar>>(){}.type)
            println("解析后：$map1")
        }

        data class MyData(val map: Map<String, Calendar>)
    }
}

package com.example.newdiary

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

// TODO: 1.改为带参（context）单例模式 2.检查逻辑顺序，重新修改保存读取方法，清晰log
class DataRepository(private val context:Context) {

//    var calendarMap = mutableMapOf<String,com.haibin.calendarview.Calendar>()
    var liveCalendarMap = MutableLiveData<MutableMap<String,com.haibin.calendarview.Calendar>>()
//    val liveCalendarMap:LiveData<MutableMap<String,com.haibin.calendarview.Calendar>> get() = _liveCalendarMap

    // 外部储存路径
    private val dataOutFilePath =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "Diary"

    // 内部储存路径
    private val dataInFilePath: String = context.filesDir.path
    private val dataFileName = "XData.json"
    private val dataFileName1 = "xDataCalendar.json"

    private val TAG = "DataRepository"


    /**
     * 保存数据到内部储存
     */
    fun inSave() {
        save(dataInFilePath)
    }

    /**
     * 读取内部储存数据
     */
    fun inRead() {
        read(dataInFilePath)
    }

    /**
     * 导出数据到指定路径
     */
    fun outData() {
        save(dataOutFilePath)
        MyToast.showMsg(context, "数据输出完成！\n$dataOutFilePath")
    }

    /**
     * 从指定路径导入数据
     */
    fun importData() {
        read(dataOutFilePath)
        inSave()
        MyToast.showMsg(context, "数据导入完成!")
    }

    /**
     * 保存数据文件基本方法
     * @param path 文件路径
     */
    private fun save(path: String) {
        var fileOutput: FileOutputStream? = null
        try {
            // 判断目录是否存在
            if (!File(path).exists()) {
                File(path).mkdirs()
            }
            val file = File(path, dataFileName)
            if (!file.exists()) {
                file.createNewFile() // 创建文件
            }

            fileOutput = FileOutputStream(file)

            // 将数据List转化为Str
//            val diaryListStr: String = Gson().toJson(calendarMapToTimeInMillisList(calendarMap))
            val diaryListStr: String = Gson().toJson(calendarMapToTimeInMillisList(getCalendarMap()))

            // 保存对象
            fileOutput.write(diaryListStr.toByteArray())
            Log.d(TAG, "save()方法: XDataList集合信息保存完成！")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "save()方法: 创建序列流或IO流失败！")
        } finally {
            if (fileOutput != null) {
                try {
                    fileOutput.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 读取文件
     */
    private fun read(path: String) {
        var xDataStr = StringBuilder()
        var fileInput: FileInputStream? = null
        try {
            val file = File(path + File.separator + dataFileName)
            // 创建输入流
            fileInput = FileInputStream(file)

            var len = 0
            val b: ByteArray = ByteArray(1024)
            while (fileInput.read(b).also { len = it } != -1){
                xDataStr.append(String(b,0,len))
            }

            val xDataList:MutableList<Long> = Gson().fromJson(xDataStr.toString(), object :TypeToken<MutableList<Long>>(){}.type)
//            calendarMap = timeInMillisListToCalendarMap(xDataList)
            liveCalendarMap = MutableLiveData(timeInMillisListToCalendarMap(xDataList))
            Log.d(TAG, "read: 数据读取完成\n$liveCalendarMap.value")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "read()方法: IO流创建失败！请检查是否第一次运行APP，如不是则找不数据文件或已损坏")
            save(dataInFilePath)
        } finally {
            // 释放资源
            if (fileInput != null) {
                try {
                    fileInput.close()
                    Log.d(TAG, "read()方法: XData.json文件释放成功！")
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d(TAG, "read()方法: XData.json文件关闭失败！")
                }
            }
        }
    }

    private fun getCalendarMap(): MutableMap<String, com.haibin.calendarview.Calendar> {
        return liveCalendarMap.value!!
    }

    /**
     * 添加数据
     * @return true成功，false失败
     */
    fun add(calendar: com.haibin.calendarview.Calendar): Boolean {
        if(!checkList(calendar)){
//            calendarMap[calendar.toString()] = calendar
            liveCalendarMap.value?.set(calendar.toString(), calendar)
            Log.d(TAG, "add: "+liveCalendarMap.value)
            inSave()
            return true
        }
        return false
    }

    /**
     * 删除数据
     * @return true成功，false失败
     */
    fun remove(calendar: com.haibin.calendarview.Calendar): Boolean {
        if(checkList(calendar)){
//            calendarMap[calendar.toString()] = calendar
            liveCalendarMap.value?.remove(calendar.toString())
            Log.d(TAG, "remove: "+liveCalendarMap.value)
            inSave()
            return true
        }
        return false
    }

    /**
     * 检查列表中是否存在指定日期，如果存在则返回该日期
     * @return
     */
    // todo orEmpty不知道是啥
    private fun checkList(calendar: com.haibin.calendarview.Calendar): Boolean {
        for (l in liveCalendarMap.value.orEmpty()) {
            if(l.value == calendar){
                return true
            }
        }
        return false
    }

    /**
     * 获取指定日期的Calendar日历类：毫秒值———Calendar日历类
     * @return
     */
    fun getCalendarFromLong(date: Long): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        return calendar
    }

    /**
     * 获取指定日期的毫秒值
     * @param year
     * @param month 指定月，按照1-12月传入参数，内部会自动将月数-1处理
     * @param day
     * @return
     */
    fun getTimeInMillis(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year,month - 1, day)
        return calendar.timeInMillis
    }

    /**
     * 毫秒值转CalendarView的Calendar
     */
    private fun timeInMillisToCalendar(long: Long):com.haibin.calendarview.Calendar{
        val date = Date().also { it.time = long }
        val calendar: Calendar = Calendar.getInstance().also { it.time = date }
        val calendar1: com.haibin.calendarview.Calendar = com.haibin.calendarview.Calendar().also {
            it.year = calendar[Calendar.YEAR]
            it.month = calendar[Calendar.MONTH+1]
            it.day = calendar[Calendar.DAY_OF_MONTH]
        }
        return calendar1
    }

    private fun calendarMapToTimeInMillisList(map: MutableMap<String,com.haibin.calendarview.Calendar>): MutableList<Long>{
        val list = mutableListOf<Long>()
        for (i in map){
            list.add(i.value.timeInMillis)
        }
        return list
    }

    private fun timeInMillisListToCalendarMap(list: MutableList<Long>):MutableMap<String,com.haibin.calendarview.Calendar>{
        val map = mutableMapOf<String,com.haibin.calendarview.Calendar>()
        for (i in list){
            val calendar:com.haibin.calendarview.Calendar = timeInMillisToCalendar(i)
            map[calendar.toString()] = calendar
        }
        return map
    }

    private fun jsonToCalendarMap(string: String):MutableMap<String,com.haibin.calendarview.Calendar>{
        return Gson().fromJson(string,object : TypeToken<MutableMap<String, com.haibin.calendarview.Calendar>>(){}.type)
    }
}

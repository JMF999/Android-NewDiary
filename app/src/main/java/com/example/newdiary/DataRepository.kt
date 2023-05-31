package com.example.newdiary

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.Calendar.Scheme
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// todo 1.改成单例模式 2.删除仓库类中所有Toast，现在所有Toast由仓库类完成，交给Activity处理
class DataRepository(private val context: Context) {
    var calendarMap = mutableMapOf<String, Calendar>()
    val warnNumbar = 4

    // 安卓11无法访问根目录，没权限
    // 外部储存路径  /storage/emulated/0/Diary
//    private val dataOutFilePath1 = Environment.getExternalStorageDirectory().absolutePath + File.separator + "Diary"

    // /storage/emulated/0/Android/data/com.example.newdiary/files
    private val dataOutFilePath = context.getExternalFilesDir(null)!!.path

    // 内部储存路径 /data/user/0/com.example.newdiary/files
    private val dataInFilePath: String = context.filesDir.path
    private val dataFileName = "newDiaryData.json"
    private val TAG = "DataRepository"

    /**
     * 保存数据到内部储存
     */
    fun inSave() {
        Log.d(TAG, "inSave文件路径: $dataInFilePath")
        newSave(dataInFilePath)
        outData()
    }

    /**
     * 读取内部储存数据
     */
    fun inRead() {
        Log.d(TAG, "inRead文件路径: $dataInFilePath")
        newRead(dataInFilePath)
    }

    /**
     * 导出数据到指定路径
     */
    fun outData() {
        if (checkExternalStorageState()) {
            // 外部存储可用
            newSave(dataOutFilePath)
            Log.d(TAG, "outData: 数据保存完成！\n$dataOutFilePath")
            MyToast.showToast(context, "保存数据完成.", false)
        } else {
            // 外部存储不可用
            Log.d(TAG, "outData: 外部储存不可用！\n$dataOutFilePath")
            MyToast.showToast(context, "外部储存不可用！\n$dataOutFilePath", true)
        }
    }

    /**
     * 从指定路径导入数据
     */
    fun importData() {
        if (checkExternalStorageState()) {
            newRead(dataOutFilePath)
            inSave()
            Log.d(TAG, "importData: 数据导入完成!")
            MyToast.showToast(context, "数据导入完成!", false)
        } else {
            // 外部存储不可用
            Log.d(TAG, "importData: 外部储存不可用！\n$dataOutFilePath")
            MyToast.showToast(context, "外部储存不可用！\n$dataOutFilePath", true)
        }
    }

    /**
     * 添加数据
     * @return true成功，false失败
     */
    fun add(calendar: Calendar): Boolean {
        if (!checkDataMap(calendar)) {
            calendarMap[calendar.toString()] = calendar
            Log.d(TAG, "add: $calendar")
            Log.d(TAG, "map: \n$calendarMap")
            inSave()
            return true
        } else {
            MyToast.showToast(context, "数据已存在！", false)
            Log.d(TAG, "add: 数据已存在")
        }
        return false
    }

    fun addScheme(calendar: Calendar, scheme: Scheme):Boolean{
        for (i in calendarMap){
            if(i.value == calendar){
                i.value.addScheme(scheme)
                Log.d(TAG, "addScheme: 添加Scheme数据 $scheme ----> $calendar")
                inSave()
                return true
            }
        }
        add(calendar)
        addScheme(calendar, scheme)
        Log.d(TAG, "addScheme: 添加Scheme数据失败 $scheme ----> $calendar")
        return false
    }

    fun removeScheme(calendar: Calendar, scheme: Scheme):Boolean{
        for(i in calendarMap){
            if(i.value == calendar){
                for(n in i.value.schemes){
                    if (n == scheme){
                        i.value.schemes.remove(scheme)
                        Log.d(TAG, "removeScheme: 移除Scheme数据 $scheme ----> $calendar")
                        return true
                    }
                }
            }
        }
        Log.d(TAG, "removeScheme: 移除Scheme数据失败 $scheme ----> $calendar")
        return false
    }

    /**
     * 删除数据
     * @return true成功，false失败
     */
    fun remove(calendar: Calendar): Boolean {
        if (checkDataMap(calendar)) {
            calendarMap.remove(calendar.toString())
            Log.d(TAG, "remove: $calendar")
//            Log.d(TAG, "remove: \n$calendarMap")
            inSave()
            return true
        } else {
            MyToast.showToast(context, "数据不存在！", false)
            Log.d(TAG, "remove: 数据不存在")
        }
        return false
    }

    fun clearAllData(): Boolean {
        calendarMap = mutableMapOf()
        inSave()
        Log.d(TAG, "clearAllData: 数据清空！")
        MyToast.showToast(context, "所有数据已清除.", false)
        return true
    }

    // todo 每次视图变化，这里会执行两次，需要查找activity问题
    /**
     * 指定月份总计有多少条数据
     */
    fun monthCount(year: Int, month: Int): Int {
        Log.d(TAG, "monthCount: 计算月份 $month")
        var count = 0
        for (i in calendarMap) {
            if (i.value.year == year && i.value.month == month) {
                count += 1
            }
        }
        Log.d(TAG, "monthCount: 总计:$count")
        return count
    }

    /**
     * 全部数据条数
     */
    fun allCount(): Int {
        return calendarMap.size
    }

    /**
     * 检查列表中是否存在指定日期
     * @return  存在rue, 否则false
     */
    private fun checkDataMap(calendar: Calendar): Boolean {
        for (l in calendarMap) {
            if (l.value == calendar) {
                return true
            }
        }
        return false
    }

    /**
     * 保存数据文件基本方法
     * @param path 文件路径
     */
    private fun newSave(path: String) {
        var fileOutput: FileOutputStream? = null
        try {
            // 判断目录文件夹是否存在
            if (!File(path).exists()) {
                File(path).mkdirs()
            }
            val file = File(path, dataFileName)
            // 判断文件是否存在
            if (!file.exists()) {
                file.createNewFile() // 创建文件
            }

            fileOutput = FileOutputStream(file)

            // 将数据List转化为Str
            val dataString: String = Gson().toJson(calendarMap)

            // 保存对象
            fileOutput.write(dataString.toByteArray())
            Log.d(TAG, "newSave: 数据写入文件完成.")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "newSave: 创建序列流或IO流失败.")
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
    private fun newRead(path: String) {
        val dataStr = StringBuilder()
        var fileInput: FileInputStream? = null
        val file = File(path + File.separator + dataFileName)
        Log.d(TAG, "newRead:文件路径: $file")

        try {
            if (file.exists()) {
                // 创建输入流
                fileInput = FileInputStream(file)

                var len: Int
                val b = ByteArray(1024)
                while (fileInput.read(b).also { len = it } != -1) {
                    dataStr.append(String(b, 0, len))
                }
                Log.d(TAG, "newRead:读取出文件dataStr： $dataStr")

                if (dataStr.isEmpty()) {
                    calendarMap = mutableMapOf()
                    Log.d(TAG, "newRead: 初始化map$calendarMap")
                } else {
                    calendarMap = Gson().fromJson(
                        dataStr.toString(),
                        object : TypeToken<MutableMap<String, Calendar>>() {}.type
                    )
                }

                Log.d(TAG, "newRead: 数据读取完成.")
            } else {
                // todo 文件不存在处理
                Log.d(TAG, "newRead: 文件不存在!")
                inSave()
            }
        } catch (e: IOException) {
            Log.d(TAG, "newRead: $e")
        } finally {
            // 释放资源
            if (fileInput != null) {
                try {
                    fileInput.close()
                    Log.d(TAG, "newRead: 数据文件文件释放成功！")
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d(TAG, "newRead: 数据文件文件关闭失败！$e")
                }
            }
        }
    }

    /**
     * 检查外部储存选线和是否可用
     */
    private fun checkExternalStorageState(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED  // 检查是否已经授权
                && Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED   // 检查外部存储是否可用
    }

    /**
     * 旧版DiaryApp数据转为NewDiary数据
     */
    fun oldDataToNewData() {
        val filename = "XData.json"
        val dataStr = StringBuilder()
        var fileInput: FileInputStream? = null
//        val file = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + "Diary" + File.separator + filename)
        val file = File(dataOutFilePath + File.separator + filename)
        Log.d(TAG, "oldDataToNewData文件路径: $file")

        try {
            if (file.exists()) {
                // 创建输入流
                fileInput = FileInputStream(file)

                var len: Int
                val b = ByteArray(1024)
                while (fileInput.read(b).also { len = it } != -1) {
                    dataStr.append(String(b, 0, len))
                }
                Log.d(TAG, "读取出文件dataStr： $dataStr")
                Log.d(TAG, "现有数据: $calendarMap")

                if (dataStr.isEmpty()) {
                    Log.d(TAG, "无数据！")
                } else {
                    val oldDataList: List<Long> = Gson().fromJson(
                        dataStr.toString(),
                        object : TypeToken<List<Long>>() {}.type
                    )
                    for (i in oldDataList) {
                        val calendar = Calendar()
                        val localDateTime =
                            LocalDateTime.ofInstant(Instant.ofEpochMilli(i), ZoneId.systemDefault())
                        calendar.year = localDateTime.year
                        calendar.month = localDateTime.monthValue
                        calendar.day = localDateTime.dayOfMonth
                        calendar.scheme = "X"
                        if (!checkDataMap(calendar)) {
//                            calendarMap[calendar.toString()] = calendar
                            calendarMap.put(calendar.toString(), calendar)
                        } else {
                            Log.d(TAG, "oldDataToNewData: $calendar 日期已存在.")
                        }
                    }
                    inSave()
                    Log.d(TAG, "oldDataToNewData: 数据迁移完成.")
                    Log.d(TAG, "现有数据: $calendarMap")
                    MyToast.showToast(context, "数据迁移完成", false)
                }
            } else {
                // 文件不存在处理
                Log.d(TAG, "oldDataToNewData: 文件不存在!")
                MyToast.showToast(context, "文件不存在！", true)
            }
        } catch (e: IOException) {
            Log.d(TAG, "oldDataToNewData: $e")
        } finally {
            // 释放资源
            if (fileInput != null) {
                try {
                    fileInput.close()
                    Log.d(TAG, "oldDataToNewData: 数据文件文件释放成功！")
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d(TAG, "oldDataToNewData: 数据文件文件关闭失败！$e")
                }
            }
        }
    }

    /**
     * 毫秒值转年月日数组
     */
    private fun millisecondsToYearMonthDayArray(milliseconds: Long): IntArray {
        val localDateTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault())
        return intArrayOf(localDateTime.year, localDateTime.monthValue, localDateTime.dayOfMonth)
    }
}

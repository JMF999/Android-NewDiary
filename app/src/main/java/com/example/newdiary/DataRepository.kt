package com.example.newdiary

import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.haibin.calendarview.Calendar
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

// todo 单例模式
class DataRepository(private val context: Context) {
    var calendarMap = mutableMapOf<String,Calendar>()

    // 外部储存路径
    private val dataOutFilePath =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "Diary"
    // 内部储存路径
    private val dataInFilePath: String = context.filesDir.path
    private val dataFileName = "XData.json"

    private val TAG = "DataRepository"

    /**
     * 保存数据到内部储存
     */
    fun inSave() {
        newSave(dataInFilePath)
    }

    /**
     * 读取内部储存数据
     */
    fun inRead() {
        newRead(dataInFilePath)
    }

    /**
     * 导出数据到指定路径
     */
    fun outData() {
        newSave(dataOutFilePath)
        Log.d(TAG, "outData: 数据输出完成！\n$dataOutFilePath")
        MyToast.showToast(context, "数据输出完成！\n$dataOutFilePath")
    }

    /**
     * 从指定路径导入数据
     */
    fun importData() {
        newRead(dataOutFilePath)
        inSave()
        Log.d(TAG, "outData: 数据导入完成!")
        MyToast.showToast(context, "数据导入完成!")
    }

    /**
     * 添加数据
     * @return true成功，false失败
     */
    fun add(calendar: Calendar): Boolean {
        if (!checkList(calendar)) {
            calendarMap[calendar.toString()] = calendar
            Log.d(TAG, "add: $calendar")
            Log.d(TAG, "map: \n$calendarMap")
            inSave()
            return true
        }else{
            MyToast.showToast(context,"数据已存在！")
            Log.d(TAG, "add: 数据已存在")
        }
        return false
    }

    /**
     * 删除数据
     * @return true成功，false失败
     */
    fun remove(calendar: Calendar): Boolean {
        if (checkList(calendar)) {
            calendarMap.remove(calendar.toString())
            Log.d(TAG, "remove: $calendar")
            Log.d(TAG, "map: \n$calendarMap")
            inSave()
            return true
        }else{
            MyToast.showToast(context,"数据不存在！")
            Log.d(TAG, "remove: 数据不存在")
        }
        return false
    }

    /**
     * 检查列表中是否存在指定日期
     * @return  存在rue, 否则false
     */
    private fun checkList(calendar: Calendar): Boolean {
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
            Log.d(TAG, "save: 数据写入文件完成.")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "save: 创建序列流或IO流失败.")
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
        Log.d(TAG, "文件路径: $file")

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

                if(dataStr.isEmpty()){
                    calendarMap = mutableMapOf()
                    Log.d(TAG, "newRead: 初始化map$calendarMap")
                }else{
                    calendarMap = Gson().fromJson(dataStr.toString(), object :TypeToken<MutableMap<String, Calendar>>(){}.type)
                }

                Log.d(TAG, "read: 数据读取完成.")
            } else {
                // todo 文件不存在处理
                Log.d(TAG, "newRead: 文件不存在!")
                inSave()
            }
        }catch (e: IOException){
            Log.d(TAG, "newRead: $e")
        }finally {
            // 释放资源
            if (fileInput != null) {
                try {
                    fileInput.close()
                    Log.d(TAG, "read方法: 数据文件文件释放成功！")
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d(TAG, "read方法: 数据文件文件关闭失败！$e")
                }
            }
        }



    }
}

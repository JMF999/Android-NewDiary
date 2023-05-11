package com.example.newdiary

import android.app.Application
import android.content.res.Configuration
import android.util.Log

class MyApplication : Application() {
    companion object{
        private lateinit var app:MyApplication
        fun getInstance(): MyApplication {
            return  app
        }
    }

    lateinit var dataRepository:DataRepository

    // 程序启动时调用
    override fun onCreate() {
        super.onCreate()
        app = this
        dataRepository = DataRepository(applicationContext)
        Log.d("MyApplication", "数据仓库初始化完成！")

        dataRepository.inRead() // 加载数据
    }

    // 程序终止时调用
    override fun onTerminate() {
        super.onTerminate()
        dataRepository.inSave()
    }

    // 低内存时
    override fun onLowMemory() {
        super.onLowMemory()
    }

    // 配置改变时
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    // 内存清理时，低内存时
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
    }
}
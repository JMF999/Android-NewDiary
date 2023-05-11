package com.example.newdiary

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.newdiary.databinding.ActivityMainBinding
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView

class MainActivity : AppCompatActivity() {
    private val TAG: String = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var today: Calendar

    private lateinit var viewModel2: MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置日历属性
        binding.calendarView.setMonthView(MeiZuMonthView::class.java)
        binding.calendarView.setWeekView(MeizuWeekView::class.java)

        // 初始化顶部文字
        upDate()
        today = binding.calendarView.selectedCalendar

        // 设置返回当前日期
        binding.mainTv1.setOnClickListener {
            binding.calendarView.scrollToCurrent(true)
            upDate()
        }
        // 设置年试图日历选择器
        binding.mainTv1.setOnLongClickListener {
            binding.calendarView.showYearSelectLayout(binding.calendarView.curYear)
            upDate()
            return@setOnLongClickListener false
        }
        // 设置年试图是否打开，隐藏悬浮按钮
        binding.calendarView.setOnYearViewChangeListener { isClose ->
            if (isClose) {
                binding.mainFloatingActionButton.visibility = View.VISIBLE
            } else {
                binding.mainFloatingActionButton.visibility = View.INVISIBLE
            }
        }
        // 设置日期选择器
        binding.calendarView.setOnCalendarSelectListener(object :
            CalendarView.OnCalendarSelectListener {
            override fun onCalendarOutOfRange(calendar: Calendar?) {

            }

            override fun onCalendarSelect(calendar: Calendar?, isClick: Boolean) {
                if (isClick) {
                    upDate(calendar)
                }
                upDate(calendar)
                today.differ(calendar).also {
                    if (it < 0) {
                        binding.mainTv5.text = (-it).toString()
                    } else {
                        binding.mainTv5.text = it.toString()
                    }
                }

            }
        })

        // 获取 MyViewModel 对象
        viewModel2 = ViewModelProvider(this)[MyViewModel::class.java]
        binding.calendarView.setSchemeDate(viewModel2.liveDataMap.value)
        viewModel2.liveDataMap.observe(this){
            // 在数据变化时触发该回调函数
            upText(it)
            binding.calendarView.setSchemeDate(it)
            binding.calendarView.update()
            Log.d(TAG, "onCreate: 数据变化！$it")
            Log.d(TAG, "onCreate: ${binding.calendarView.selectedCalendar.scheme}")
        }

        // 设置日历长时间单击侦听器
        binding.calendarView.setOnCalendarLongClickListener ( object :CalendarView.OnCalendarLongClickListener {
            override fun onCalendarLongClickOutOfRange(calendar: Calendar?) {

            }

            override fun onCalendarLongClick(calendar: Calendar?) {
                if(calendar?.hasScheme() == true){
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("删除当前选择的日期")
                        .setPositiveButton("确认") { _, _ ->
                            viewModel2.remove(binding.calendarView.selectedCalendar)
                        }.setNegativeButton("取消") { _, _ -> }.show()
                }else if (calendar?.hasScheme() == false) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("添加当前选择的日期")
                        .setPositiveButton("确认") { _, _ ->
                            Log.d(TAG, "将"+binding.calendarView.selectedCalendar+"添加至map")
                            binding.calendarView.selectedCalendar.scheme = "X"
                            viewModel2.add(binding.calendarView.selectedCalendar)
                        }.setNegativeButton("取消") { _, _ -> }.show()
                }

            }
        })

        // 主浮动动作按钮
        binding.mainFloatingActionButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("添加当前选择的日期")
                .setPositiveButton("确认") { _, _ ->
                    val calendar = binding.calendarView.selectedCalendar
                    viewModel2.add(calendar)
                }.setNegativeButton("取消") { _, _ -> }.show()
        }
    }

    private fun upDate() {
        val s: String =
            binding.calendarView.curMonth.toString() + "月" + binding.calendarView.curDay.toString() + "日"
        binding.mainTv1.text = s
        binding.mainTv2.text = binding.calendarView.selectedCalendar.year.toString()
        binding.mainTv3.text = binding.calendarView.selectedCalendar.lunar
    }

    private fun upDate(calendar: Calendar?) {
        if (calendar != null) {
            val s: String = calendar.month.toString() + "月" + calendar.day.toString() + "日"
            binding.mainTv1.text = s
            binding.mainTv2.text = calendar.year.toString()
            binding.mainTv3.text = calendar.lunar
        }
    }

    private fun upText(map:Map<String, Calendar>){
        val s = StringBuilder()
        for (i in map){
            s.append(i.key +"=" + i.value + "\n")
        }
        binding.textView8.text = s
    }

    private fun copyCalendar(oldCalendar: Calendar): Calendar {
        return Calendar().also {
            it.year = oldCalendar.year
            it.month = oldCalendar.month
            it.day = oldCalendar.day
            it.lunar = oldCalendar.lunar
        }
    }

}
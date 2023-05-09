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

    private var dataRepository = MyApplication.getInstance().dataRepository

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
        binding.textView2.setOnClickListener {
            binding.calendarView.scrollToCurrent(true)
            upDate()
        }
        // 设置年试图日历选择器
        binding.textView2.setOnLongClickListener {
            binding.calendarView.showYearSelectLayout(binding.calendarView.curYear)
            upDate()
            return@setOnLongClickListener false
        }
        // 设置年试图是否打开，隐藏悬浮按钮
        binding.calendarView.setOnYearViewChangeListener { isClose ->
            if (isClose) {
                binding.floatingActionButton.visibility = View.VISIBLE
            } else {
                binding.floatingActionButton.visibility = View.INVISIBLE
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
                        binding.textView10.text = (-it).toString()
                    } else {
                        binding.textView10.text = it.toString()
                    }
                }

            }
        })

        val myViewModel = ViewModelProvider(this)[MyViewModel::class.java]
        upText(myViewModel.liveCalendarMap.value!!)
        binding.calendarView.setSchemeDate(myViewModel.liveCalendarMap.value)
        myViewModel.liveCalendarMap.observe(this){
            upText(it)
            binding.calendarView.setSchemeDate(it)
            binding.calendarView.update()
            Log.d(TAG, "onCreate: 数据变化！$it")
        }
//        val map:HashMap<String, Calendar> = dataRepository.liveCalendarMap.value as HashMap<String, Calendar>
//        dataRepository.liveCalendarMap.observe(this){
////            upText(dataRepository.getCalendarMap())
//            upText(it)
//            binding.calendarView.setSchemeDate(it)
////            binding.calendarView.setSchemeDate(dataRepository.getCalendarMap())
//            binding.calendarView.update()
//            Log.d(TAG, "onCreate: 数据变化！$it")
//        }

        binding.calendarView.setOnCalendarLongClickListener ( object :CalendarView.OnCalendarLongClickListener {
            override fun onCalendarLongClickOutOfRange(calendar: Calendar?) {

            }

            override fun onCalendarLongClick(calendar: Calendar?) {
                if(calendar?.hasScheme() == true){
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("删除当前选择的日期")
                        .setPositiveButton("确认") { _, _ ->
//                            binding.calendarView.removeSchemeDate(binding.calendarView.selectedCalendar)
                            dataRepository.remove(binding.calendarView.selectedCalendar)
//                            upText(map)
                        }.setNegativeButton("取消") { _, _ -> }.show()
                }else if (calendar?.hasScheme() == false) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("添加当前选择的日期")
                        .setPositiveButton("确认") { _, _ ->
                            dataRepository.add(binding.calendarView.selectedCalendar)
//                            binding.calendarView.addSchemeDate(binding.calendarView.selectedCalendar)
//                            upText(map)
                        }.setNegativeButton("取消") { _, _ -> }.show()
                }

            }
        })


//        map[today.toString()] = today
//
//        val calendar = copyCalendar(today)
//        calendar.day = calendar.day - 1
//        calendar.scheme = "-1"
//        map[calendar.toString()] = calendar
//
//        val calendar1 = copyCalendar(today)
//        calendar1.day = calendar1.day - 2
//        calendar1.scheme = "-2"
//        map["日期"] = calendar1


//        binding.calendarView.setSchemeDate(dataRepository.calendarMap)

////        binding.calendarView.addSchemeDate(calendar)
////        binding.calendarView.update()
//        Log.d("MainActivity", "onCreate: ============" + calendar)
//        Log.d("MainActivity", "onCreate: ============" + today)

//        binding.floatingActionButton.setOnClickListener {
//            DatePickerDialog(this).apply {
//                this.setOnDateSetListener { view, year, month, dayOfMonth ->
//                    binding.calendarView.addSchemeDate(Calendar().also {
//                        it.year = year
//                    it.month = month+1
//                    it.day = dayOfMonth
//                        Log.d("MainActivity", "添加: " + it)})
//                }
//                this.show()
//            }
//        }

        binding.floatingActionButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("添加当前选择的日期")
                .setPositiveButton("确认") { _, _ ->
                    val calendar = binding.calendarView.selectedCalendar
                    dataRepository.add(calendar)
//                    binding.calendarView.addSchemeDate(calendar)
                    Log.d(TAG, "添加日期: " + dataRepository.liveCalendarMap.value)
                }.setNegativeButton("取消") { _, _ -> }.show()
        }
    }

    private fun upDate() {
        val s: String =
            binding.calendarView.curMonth.toString() + "月" + binding.calendarView.curDay.toString() + "日"
        binding.textView2.text = s
        binding.textView7.text = binding.calendarView.selectedCalendar.year.toString()
        binding.textView5.text = binding.calendarView.selectedCalendar.lunar
    }

    private fun upDate(calendar: Calendar?) {
        if (calendar != null) {
            val s: String = calendar.month.toString() + "月" + calendar.day.toString() + "日"
            binding.textView2.text = s
            binding.textView7.text = calendar.year.toString()
            binding.textView5.text = calendar.lunar
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
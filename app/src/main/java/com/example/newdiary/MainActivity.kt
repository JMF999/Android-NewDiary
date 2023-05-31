package com.example.newdiary

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.newdiary.databinding.ActivityMainBinding
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.Calendar.Scheme
import com.haibin.calendarview.CalendarView


class MainActivity : AppCompatActivity() {
    private val TAG: String = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var today: Calendar

    private lateinit var viewModel: MyViewModel
    private var nowMonthCount: Int = 0  // 当前显示月份的数据总数，为了避免重复计算而建立
    private var todayMonthCount: Int = 0  // 当前实际月份的数据总数

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setDecorFitsSystemWindows(false) // 沉浸式状态栏相关。通知视窗，我们（应用）会处理任何系统视窗（而不是 decor）
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.materialToolbar)    // 设置状态栏

        // 申请权限
        checkSelfPermission()
        today = binding.calendarView.selectedCalendar

        // 获取 MyViewModel 对象
        viewModel = ViewModelProvider(this)[MyViewModel::class.java]
        // viewModel数据观察
        binding.calendarView.setSchemeDate(viewModel.liveDataMap.value)
        // 在数据变化时触发该回调函数,同时会初始化界面文本
        viewModel.liveDataMap.observe(this) {
            // 更新当月总计次数
            nowMonthCount = viewModel.monthCount(
                binding.calendarView.selectedCalendar.year,
                binding.calendarView.selectedCalendar.month
            )
            todayMonthCount = viewModel.monthCount(
                today.year,
                today.month
            )

//            if (nowMonthCount > viewModel.warnNumber) {
//                viewModel.switchBackground.value = 5
//            }    // 判断是否更换背景
            invalidateOptionsMenu()     // 刷新标题栏

            // 更新界面文字信息
//            upTextMap(it)
            upTextInformation()
            upTitleDate()

            // 重新为日历view设置数据列表
            binding.calendarView.setSchemeDate(it)
            binding.calendarView.update()   // 更新日历显示

            Log.d(TAG, "onCreate: 数据变化！$it")
        }

        // 观察背景图片数据，切换主题
        viewModel.switchBackground.observe(this) {
            Log.d(TAG, "viewModel.switchBackground.observe: $it")
            when (it) {
                0 -> binding.mainRoot.background =
                    AppCompatResources.getDrawable(this, R.color.calendar_background)
                1 -> binding.mainRoot.background =
                    AppCompatResources.getDrawable(this, R.drawable.background1)
                2 -> binding.mainRoot.background =
                    AppCompatResources.getDrawable(this, R.drawable.background2)
                3 -> binding.mainRoot.background =
                    AppCompatResources.getDrawable(this, R.drawable.background3)
                4 -> binding.mainRoot.background =
                    AppCompatResources.getDrawable(this, R.drawable.background4)
                5 -> binding.mainRoot.background =
                    AppCompatResources.getDrawable(this, R.drawable.background_red)
            }
            if (it > 0) {
                binding.calendarView.background =
                    AppCompatResources.getDrawable(
                        this,
                        R.drawable.main_information_background_alp30
                    )
                binding.scrollView2.background =
                    AppCompatResources.getDrawable(
                        this,
                        R.drawable.main_information_background_alp30
                    )
            } else {
                binding.calendarView.background =
                    AppCompatResources.getDrawable(this, R.drawable.main_information_background)
                binding.scrollView2.background =
                    AppCompatResources.getDrawable(this, R.drawable.main_information_background)
            }
        }

        // 设置日历属性
        binding.calendarView.setMonthView(MeiZuMonthView::class.java)
        binding.calendarView.setWeekView(MeizuWeekView::class.java)

        // 设置返回当前日期
        binding.mainTv1.setOnClickListener {
            binding.calendarView.scrollToCurrent(true)
            upTitleDate()
            upTextInformation()
        }
        // 设置年试图日历选择器
        binding.mainTv1.setOnLongClickListener {
            binding.calendarView.showYearSelectLayout(binding.calendarView.curYear)
            upTitleDate()
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
                nowMonthCount = viewModel.monthCount(
                    binding.calendarView.selectedCalendar.year,
                    binding.calendarView.selectedCalendar.month
                )
                upTitleDate()
                upTextInformation()
            }
        })

        // 设置日历长时间单击侦听器
        binding.calendarView.setOnCalendarLongClickListener(object :
            CalendarView.OnCalendarLongClickListener {
            override fun onCalendarLongClickOutOfRange(calendar: Calendar?) {

            }

            override fun onCalendarLongClick(calendar: Calendar?) {
                if (calendar?.hasScheme() == true) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("删除当前选择的日期")
                        .setPositiveButton("确认") { _, _ ->
                            viewModel.remove(binding.calendarView.selectedCalendar)
                        }.setNegativeButton("取消") { _, _ -> }.show()
                } else if (calendar?.hasScheme() == false) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("添加当前选择的日期")
                        .setPositiveButton("确认") { _, _ ->
                            Log.d(TAG, "将" + binding.calendarView.selectedCalendar + "添加至map")
                            binding.calendarView.selectedCalendar.scheme = "X"
                            viewModel.add(binding.calendarView.selectedCalendar)
                        }.setNegativeButton("取消") { _, _ -> }.show()
                }

            }
        })

        // 主浮动动作按钮
        binding.mainFloatingActionButton.setOnClickListener {
            addDialog()
        }
    }

    /**
     * 切换主题,单选对话框
     */
    private fun switchBackground() {
        val options = arrayOf("原始背景", "紫绿", "粉白", "蓝紫", "蓝色")
        var selectedOptionIndex = viewModel.switchBackground.value ?: 0 // 默认选中的选项索引

        val builder = AlertDialog.Builder(this)
        builder.setTitle("更换背景")
        builder.setSingleChoiceItems(options, selectedOptionIndex) { _, which ->
            selectedOptionIndex = which // 更新选中的选项索引
        }

        builder.setPositiveButton("确定") { _, _ ->
            // 在确定按钮点击时执行的逻辑，可以根据选中的选项索引执行相应操作
            viewModel.switchBackground.value = selectedOptionIndex
        }

        builder.setNegativeButton("取消", null) // 取消按钮

        val dialog = builder.create()
        dialog.show()
    }

    /**
     * 设置标题栏bar
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        updateMenuIcon()
        return super.onCreateOptionsMenu(menu)

    }

    // 在需要更新菜单图标的地方调用该方法
    private fun updateMenuIcon() {
        Log.d(TAG, "updateMenuIcon: 更改toolbar")
        // 修改菜单项的图标
        binding.materialToolbar.menu.findItem(R.id.main_menu_7).isVisible =
            todayMonthCount > viewModel.warnNumber
        // 更新菜单显示
//        setSupportActionBar(binding.materialToolbar)
//        invalidateOptionsMenu()
    }

    /**
     * 右上角菜单栏监听
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.main_menu_1 -> AlertDialog.Builder(this@MainActivity)
                .setTitle("导入数据？")
                .setPositiveButton("确认") { _, _ ->
                    viewModel.importData()
//                    MyToast.showToast(this,"导入数据完成.")
                }.setNegativeButton("取消") { _, _ -> }.show()
            R.id.main_menu_2 -> AlertDialog.Builder(this@MainActivity)
                .setTitle("导出数据？")
                .setPositiveButton("确认") { _, _ ->
                    viewModel.outData()
//                    MyToast.showToast(this,"导出数据完成.")
                }.setNegativeButton("取消") { _, _ -> }.show()
            R.id.main_menu_3 -> AlertDialog.Builder(this@MainActivity)
                .setTitle("清空所有数据？！")
                .setPositiveButton("确认") { _, _ ->
                    viewModel.clearAllData()
//                    MyToast.showToast(this,"所有数据已清除.")
                }.setNegativeButton("取消") { _, _ -> }.show()
            R.id.main_menu_4 -> AlertDialog.Builder(this@MainActivity)
                .setTitle("旧版DiaryAPP数据导入迁移")
                .setMessage("请将数据文件放置在手机根目录Diary文件夹下，并确定文件名为XData.json")
                .setPositiveButton("开始导入") { _, _ ->
                    viewModel.oldDataToNewData()
                }.setNegativeButton("取消") { _, _ -> }.show()
            R.id.main_menu_5 -> switchBackground()
            R.id.main_menu_7 -> AlertDialog.Builder(this@MainActivity)
                .setTitle("警告！")
                .setMessage("本月记录次数已经超过阈值！ ${viewModel.monthCount(today.year, today.month)}")
                .setPositiveButton("确认") { _, _ -> }.show()
            R.id.main_menu_6 -> menuDisplayData()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun menuDisplayData() {
        val builder = AlertDialog.Builder(this)
        val view: View =
            LayoutInflater.from(this).inflate(R.layout.layout_main_all_data_display, null)
        val tv: TextView = view.findViewById<EditText>(R.id.dialog_dataDisplay_content)
        tv.text = "历史总计：${viewModel.allCount()}\n" + upTextMap(viewModel.liveDataMap.value!!)
        val dialog = builder.setView(view).show()

        // 设置自定义宽度
        val layoutParams = WindowManager.LayoutParams()
        val window = dialog.window
        layoutParams.copyFrom(window?.attributes)
        layoutParams.width = resources.getDimensionPixelSize(R.dimen.dialog_width) // 自定义宽度
        layoutParams.height = resources.getDimensionPixelSize(R.dimen.dialog_height) // 自定义高度
        window?.attributes = layoutParams
    }


    /**
     * 更新标题文字
     */
    @SuppressLint("SetTextI18n")
    private fun upTitleDate() {
        binding.mainTv1.text = binding.calendarView.selectedCalendar.month.toString() + "月"
        binding.mainTv2.text = binding.calendarView.selectedCalendar.year.toString()
//        binding.mainTv2.text = binding.calendarView.selectedCalendar.lunar
        binding.mainTv3.text = distanceToday(binding.calendarView.selectedCalendar)
        if (nowMonthCount > 4) {
            binding.mainTv9.setTextColor(getColor(R.color.red))
        } else {
            binding.mainTv9.setTextColor(getColor(R.color.calendar_text))
        }
        binding.mainTv9.text = nowMonthCount.toString()
    }

    /**
     * 更新信息窗口内容，全部数据
     */
    private fun upTextMap(map: Map<String, Calendar>): String {
        val s = StringBuilder()
        s.append("全部数据:\n")
        for (i in map) {
            s.append("${i.key}\n")
        }
        s.append("\n")
//        binding.mainMapAllText.text = s
        return s.toString()
    }

    /**
     * 更新信息窗口内容，常规信息
     */
    @SuppressLint("SetTextI18n")
    private fun upTextInformation() {
        binding.mainCalendarContent.text =
            "${binding.calendarView.selectedCalendar.year} ${binding.calendarView.selectedCalendar.month} ${binding.calendarView.selectedCalendar.day} " +
                    "\nscheme:${binding.calendarView.selectedCalendar.scheme} schemeColor:${binding.calendarView.selectedCalendar.schemeColor}" +
                    "\nlunar:${binding.calendarView.selectedCalendar.lunar} " +
                    "\nlunarCalendar:${binding.calendarView.selectedCalendar.lunarCalendar?.lunar} " +
                    "\ntraditionFestival:${binding.calendarView.selectedCalendar.traditionFestival} " +
                    "\ngregorianFestival:${binding.calendarView.selectedCalendar.gregorianFestival}" +
                    "\nsolarTerm:${binding.calendarView.selectedCalendar.solarTerm}\n"
//                "\n${schemesToStr(binding.calendarView.selectedCalendar.schemes)}\n"
        if (binding.calendarView.selectedCalendar.schemes != null) {
            binding.mainCalendarContent.text =
                binding.mainCalendarContent.text.toString() + "${schemesToStr(binding.calendarView.selectedCalendar.schemes)}\n"
        }

        var distance: Int
        today.differ(binding.calendarView.selectedCalendar).also {
            distance = if (it < 0) {
                (-it)
            } else {
                it
            }
        }
        binding.mainMapCount.text =
            "距离今天：${distance}\n历史总计：${viewModel.allCount()}\n当月总计：${nowMonthCount}\n"
    }

    /**
     * 计算与本日相差时间
     */
    private fun distanceToday(calendar: Calendar): String {
        today.differ(calendar).also {
            return if (it == 0) {
                "今天"
            } else if (it < 0) {
                "${(-it)}日后"
            } else {
                "${(it)}日前"
            }
        }
    }

    /**
     * 申请权限
     */
    private fun checkSelfPermission(): Boolean {
        // 检查是否已经授权
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 如果未授权，则向用户请求授权
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1
            )
        } else {
            return true
        }
        return false
    }

    /**
     * 申请权限回调
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            MyToast.showToast(this, "已获取权限！", false)
        } else {
            MyToast.showToast(this, "请授予储存权限，否则数据导入导出功能将无法使用！", true)
            checkSelfPermission()
        }
    }

    /**
     * 创建添加数据dialog
     */
    private fun addDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View =
            LayoutInflater.from(this).inflate(R.layout.layout_main_add_dialog, null)
        val ed1 = view.findViewById<EditText>(R.id.add_dialog_ed1)
//        val ed2 = view.findViewById<EditText>(R.id.add_dialog_ed2)
        val ed3 = view.findViewById<EditText>(R.id.add_dialog_ed3)
//        val ed4 = view.findViewById<EditText>(R.id.add_dialog_ed4)
//        val ed5 = view.findViewById<EditText>(R.id.add_dialog_ed5)
        val bnYes = view.findViewById<Button>(R.id.add_dialog_btn1)
        val bnNo = view.findViewById<Button>(R.id.add_dialog_btn2)
        val alertDialog = builder.setView(view).show()
        bnYes.setOnClickListener {
            val scheme = Scheme()
            if (ed1.text.toString() != "") {
                scheme.type = ed1.text.toString().toInt()
            }
            if (ed3.text.toString() != "") {
                scheme.scheme = ed3.text.toString()
            }
            viewModel.addScheme(binding.calendarView.selectedCalendar, scheme)
            alertDialog.dismiss()
        }
        bnNo.setOnClickListener { alertDialog.dismiss() }
    }

    private fun schemeToStr(scheme: Scheme): String {
        return "type: ${scheme.type}\nshcemeColor: ${scheme.shcemeColor}\nscheme: ${scheme.scheme}\nother: ${scheme.other}\n obj: ${scheme.obj}"
    }

    private fun schemesToStr(schemes: List<Scheme>): String {
        val s = StringBuilder()
        for (i in schemes) {
            s.append(schemeToStr(i))
        }
        return s.toString()
    }

    /**
     * 沉浸式状态栏相关，具体不知道干啥的
     */
    override fun onResume() {
        super.onResume()
        val insetsController = window.insetsController
        if (insetsController != null) {
            // 系统为深色模式
            if (isDarkModeEnabled()) {
                insetsController.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
            insetsController.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    /**
     * 判断系统是否为深色模式
     */
    private fun isDarkModeEnabled(): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}
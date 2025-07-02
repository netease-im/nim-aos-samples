package com.netease.nim.samples.login.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentAddLoginListenerBinding
import com.netease.nim.samples.login.common.GlobalLoginListenerManager
import java.text.SimpleDateFormat
import java.util.*

class AddLoginListenerFragment : BaseMethodExecuteFragment<FragmentAddLoginListenerBinding>() {

    /**
     * 计时器Handler
     */
    private val timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    /**
     * 时间格式化器
     */
    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAddLoginListenerBinding {
        return FragmentAddLoginListenerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        updateUI()
        startTimer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer()
    }

    private fun initClickListeners() {

        // 清空历史记录
        binding.btnClearHistory.setOnClickListener {
            clearHistory()
        }
    }

    /**
     * 添加登录监听器 - 调用全局监听器管理器
     */
    private fun addLoginListener() {
        if (GlobalLoginListenerManager.isListenerAdded()) {
            showToast("全局监听器已经添加，无需重复添加")
            return
        }

        val success = GlobalLoginListenerManager.addListener(requireContext())
        if (success) {
            showToast("✅ 全局登录监听器添加成功")
            updateUI()
        } else {
            showToast("❌ 添加监听器失败")
        }
    }

    /**
     * 清空历史记录
     */
    private fun clearHistory() {
        GlobalLoginListenerManager.clearHistory()
        updateUI()
        showToast("已清空所有历史记录")
    }

    /**
     * 更新UI显示
     */
    private fun updateUI() {
        activity?.runOnUiThread {
            updateListenerStatus()
            updateStatistics()
            updateHistoryDisplay()
        }
    }

    /**
     * 更新监听器状态显示
     */
    private fun updateListenerStatus() {
        if (GlobalLoginListenerManager.isListenerAdded()) {
            binding.tvListenerStatus.text = "● 全局监听器已添加 (应用级别生效)"
            binding.tvListenerStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        } else {
            binding.tvListenerStatus.text = "● 未添加全局监听器"
            binding.tvListenerStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        }

        // 更新最新回调显示
        val latestCallback = GlobalLoginListenerManager.getLatestCallback()
        if (latestCallback != null) {
            val timeStr = timeFormatter.format(Date(latestCallback.timestamp))
            val latestText = "[$timeStr] ${latestCallback.eventType} - ${latestCallback.eventDetail}\n${latestCallback.eventData}"
            binding.tvLatestCallback.text = latestText
        } else {
            binding.tvLatestCallback.text = "暂无回调"
        }
    }

    /**
     * 更新统计信息
     */
    private fun updateStatistics() {
        val statistics = GlobalLoginListenerManager.getStatistics()
        val statisticsText = "总回调次数: ${statistics.totalCallbackCount}\n" +
                "登录状态: ${statistics.loginStatusCount} | 登录失败: ${statistics.loginFailedCount} | 被踢下线: ${statistics.kickedOfflineCount} | 客户端变更: ${statistics.clientChangedCount}"
        binding.tvCallbackStatistics.text = statisticsText
    }

    /**
     * 更新历史记录显示
     */
    private fun updateHistoryDisplay() {
        val history = GlobalLoginListenerManager.getCallbackHistory()

        if (history.isEmpty()) {
            binding.tvCallbackHistory.text = "暂无历史记录\n\n请先添加全局监听器，然后进行登录相关操作来触发回调事件"
            return
        }

        val sb = StringBuilder()
        val displayCount = minOf(20, history.size) // 显示最新20条

        for (i in 0 until displayCount) {
            val record = history[i]
            val timeStr = dateTimeFormatter.format(Date(record.timestamp))

            sb.append("[$timeStr] ${record.eventType}\n")
            sb.append("事件: ${record.eventDetail}\n")
            if (record.eventData.isNotEmpty()) {
                sb.append("详情: ${record.eventData}\n")
            }
            sb.append("${"-".repeat(40)}\n")
        }

        if (history.size > 20) {
            sb.append("... 还有${history.size - 20}条历史记录\n")
        }

        binding.tvCallbackHistory.text = sb.toString()
    }

    /**
     * 更新监听时长显示
     */
    private fun updateListenTime() {
        val listenStartTime = GlobalLoginListenerManager.getListenStartTime()
        if (GlobalLoginListenerManager.isListenerAdded() && listenStartTime > 0) {
            val duration = (System.currentTimeMillis() - listenStartTime) / 1000
            val hours = duration / 3600
            val minutes = (duration % 3600) / 60
            val seconds = duration % 60

            binding.tvListenerTime.text = String.format("监听时长: %02d:%02d:%02d", hours, minutes, seconds)
        } else {
            binding.tvListenerTime.text = "监听时长: 00:00:00"
        }
    }

    /**
     * 启动计时器
     */
    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                updateListenTime()
                updateUI() // 定期刷新UI以显示最新的回调记录
                timerHandler.postDelayed(this, 1000) // 每秒更新一次
            }
        }
        timerHandler.post(timerRunnable!!)
    }

    /**
     * 停止计时器
     */
    private fun stopTimer() {
        timerRunnable?.let {
            timerHandler.removeCallbacks(it)
        }
    }

    /**
     * 重写onRequest方法，这里主要用于显示当前状态
     */
    override fun onRequest() {
        val statistics = GlobalLoginListenerManager.getStatistics()
        val listenStartTime = GlobalLoginListenerManager.getListenStartTime()

        addLoginListener()

        val statusText = buildString {
            append("全局登录监听器状态:\n")
            append("是否已添加: ${if (GlobalLoginListenerManager.isListenerAdded()) "是" else "否"}\n")
            append("监听开始时间: ${if (listenStartTime > 0) dateTimeFormatter.format(Date(listenStartTime)) else "未开始"}\n")
            append("总回调次数: ${statistics.totalCallbackCount}\n")
            append("历史记录数量: ${GlobalLoginListenerManager.getCallbackHistory().size}\n\n")
        }

        activityViewModel.refresh(statusText)
    }
}
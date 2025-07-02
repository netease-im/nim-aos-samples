package com.netease.nim.samples.message.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentAddMessageListenerBinding
import com.netease.nim.samples.message.common.GlobalMessageListenerManager
import java.text.SimpleDateFormat
import java.util.*

class AddMessageListenerFragment : BaseMethodExecuteFragment<FragmentAddMessageListenerBinding>() {

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
    ): FragmentAddMessageListenerBinding {
        return FragmentAddMessageListenerBinding.inflate(inflater, container, false)
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
     * 添加消息监听器 - 调用全局监听器管理器
     */
    private fun addMessageListener() {
        if (GlobalMessageListenerManager.isListenerAdded()) {
            showToast("全局监听器已经添加，无需重复添加")
            return
        }

        val success = GlobalMessageListenerManager.addListener(requireContext())
        if (success) {
            showToast("✅ 全局消息监听器添加成功")
            updateUI()
        } else {
            showToast("❌ 添加监听器失败")
        }
    }

    /**
     * 清空历史记录
     */
    private fun clearHistory() {
        GlobalMessageListenerManager.clearHistory()
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
        if (GlobalMessageListenerManager.isListenerAdded()) {
            binding.tvListenerStatus.text = "● 全局监听器已添加 (应用级别生效)"
            binding.tvListenerStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        } else {
            binding.tvListenerStatus.text = "● 未添加全局监听器"
            binding.tvListenerStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        }

        // 更新最新回调显示
        val latestCallback = GlobalMessageListenerManager.getLatestCallback()
        if (latestCallback != null) {
            val timeStr = timeFormatter.format(Date(latestCallback.timestamp))
            val latestText = "[$timeStr] ${latestCallback.eventType} - ${latestCallback.eventDetail}\n${latestCallback.eventData.take(100)}${if (latestCallback.eventData.length > 200) "..." else ""}"
            binding.tvLatestCallback.text = latestText
        } else {
            binding.tvLatestCallback.text = "暂无回调"
        }
    }

    /**
     * 更新统计信息
     */
    private fun updateStatistics() {
        val statistics = GlobalMessageListenerManager.getStatistics()
        val statisticsText = "总回调次数: ${statistics.totalCallbackCount}\n" +
                "接收消息: ${statistics.receiveMessagesCount} | 发送消息: ${statistics.sendMessageCount} | 撤回通知: ${statistics.revokeNotificationsCount} | 删除通知: ${statistics.deleteNotificationsCount} | 已读回执: ${statistics.readReceiptsCount}"
        binding.tvCallbackStatistics.text = statisticsText
    }

    /**
     * 更新历史记录显示
     */
    private fun updateHistoryDisplay() {
        val history = GlobalMessageListenerManager.getCallbackHistory()

        if (history.isEmpty()) {
            binding.tvCallbackHistory.text = "暂无历史记录\n\n请先添加全局监听器，然后进行消息发送、接收等操作来触发回调事件"
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
                val displayData = if (record.eventData.length > 200) {
                    record.eventData.take(200) + "..."
                } else {
                    record.eventData
                }
                sb.append("详情: $displayData\n")
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
        val listenStartTime = GlobalMessageListenerManager.getListenStartTime()
        if (GlobalMessageListenerManager.isListenerAdded() && listenStartTime > 0) {
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
        val statistics = GlobalMessageListenerManager.getStatistics()
        val listenStartTime = GlobalMessageListenerManager.getListenStartTime()

        addMessageListener()

        val statusText = buildString {
            append("全局消息监听器状态:\n")
            append("是否已添加: ${if (GlobalMessageListenerManager.isListenerAdded()) "是" else "否"}\n")
            append("监听开始时间: ${if (listenStartTime > 0) dateTimeFormatter.format(Date(listenStartTime)) else "未开始"}\n")
            append("总回调次数: ${statistics.totalCallbackCount}\n")
            append("历史记录数量: ${GlobalMessageListenerManager.getCallbackHistory().size}\n\n")
        }

        activityViewModel.refresh(statusText)
    }
}
package com.netease.nim.samples.user.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentAddUserListenerBinding
import com.netease.nim.samples.user.common.GlobalUserListenerManager
import java.text.SimpleDateFormat
import java.util.*

class AddUserListenerFragment : BaseMethodExecuteFragment<FragmentAddUserListenerBinding>() {

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
    ): FragmentAddUserListenerBinding {
        return FragmentAddUserListenerBinding.inflate(inflater, container, false)
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
     * 添加用户监听器 - 调用全局监听器管理器
     */
    private fun addUserListener() {
        if (GlobalUserListenerManager.isListenerAdded()) {
            showToast("全局监听器已经添加，无需重复添加")
            return
        }

        val success = GlobalUserListenerManager.addListener(requireContext())
        if (success) {
            showToast("✅ 全局用户监听器添加成功")
            updateUI()
        } else {
            showToast("❌ 添加监听器失败")
        }
    }

    /**
     * 清空历史记录
     */
    private fun clearHistory() {
        GlobalUserListenerManager.clearHistory()
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
        if (GlobalUserListenerManager.isListenerAdded()) {
            binding.tvListenerStatus.text = "● 全局监听器已添加 (应用级别生效)"
            binding.tvListenerStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        } else {
            binding.tvListenerStatus.text = "● 未添加全局监听器"
            binding.tvListenerStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        }

        // 更新最新回调显示
        val latestCallback = GlobalUserListenerManager.getLatestCallback()
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
        val statistics = GlobalUserListenerManager.getStatistics()
        val statisticsText = "总回调次数: ${statistics.totalCallbackCount}\n" +
                "用户资料变更: ${statistics.userProfileChangeCount} | 黑名单添加: ${statistics.blockListAddedCount} | 黑名单移除: ${statistics.blockListRemovedCount}"
        binding.tvCallbackStatistics.text = statisticsText
    }

    /**
     * 更新历史记录显示
     */
    private fun updateHistoryDisplay() {
        val history = GlobalUserListenerManager.getCallbackHistory()

        if (history.isEmpty()) {
            binding.tvCallbackHistory.text = "暂无历史记录\n\n请先添加全局监听器，然后进行用户相关操作来触发回调事件"
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
        val listenStartTime = GlobalUserListenerManager.getListenStartTime()
        if (GlobalUserListenerManager.isListenerAdded() && listenStartTime > 0) {
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
        val statistics = GlobalUserListenerManager.getStatistics()
        val listenStartTime = GlobalUserListenerManager.getListenStartTime()

        addUserListener()

        val statusText = buildString {
            append("全局用户监听器状态:\n")
            append("是否已添加: ${if (GlobalUserListenerManager.isListenerAdded()) "是" else "否"}\n")
            append("监听开始时间: ${if (listenStartTime > 0) dateTimeFormatter.format(Date(listenStartTime)) else "未开始"}\n")
            append("总回调次数: ${statistics.totalCallbackCount}\n")
            append("历史记录数量: ${GlobalUserListenerManager.getCallbackHistory().size}\n\n")

            append("监听器回调事件说明:\n")
            append("• onUserProfileChanged: 用户资料变更通知\n")
            append("• onBlockListAdded: 用户添加到黑名单通知\n")
            append("• onBlockListRemoved: 用户从黑名单移除通知\n\n")

            append("触发回调的操作:\n")
            append("• 更新自己的用户资料 → 用户资料变更\n")
            append("• 接收他人的资料变更推送 → 用户资料变更\n")
            append("• 添加用户到黑名单 → 黑名单添加\n")
            append("• 从黑名单移除用户 → 黑名单移除\n")
            append("• 接收相关的服务端推送 → 对应事件回调\n\n")

            append("使用建议:\n")
            append("• 全局监听器建议在应用启动时添加\n")
            append("• 在应用退出时移除监听器\n")
            append("• 可以通过回调事件实时更新UI状态\n")
            append("• 监听器是全局生效的，避免重复添加")
        }

        activityViewModel.refresh(statusText)
    }
}
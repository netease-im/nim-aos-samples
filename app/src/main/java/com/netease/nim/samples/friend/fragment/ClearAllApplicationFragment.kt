package com.netease.nim.samples.friend.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentClearAllApplicationBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.friend.V2NIMFriendService
import java.text.SimpleDateFormat
import java.util.*

class ClearAllApplicationFragment : BaseMethodExecuteFragment<FragmentClearAllApplicationBinding>() {

    /**
     * 时间格式化器
     */
    private val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentClearAllApplicationBinding {
        return FragmentClearAllApplicationBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    /**
     * 发起请求
     */
    override fun onRequest() {

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMFriendService::class.java).clearAllAddApplication(
            { unreadCount -> updateResult( null) },
            { error -> updateResult( error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (error != null) {
            activityViewModel.refresh("好友申请清理失败: $error")
            return
        }

            val resultText = buildResultText()
            activityViewModel.refresh(resultText)
    }

    /**
     * 构建结果文本
     */
    private fun buildResultText(): String {
        val sb = StringBuilder()
        
        sb.append("好友申请清理成功:\n")

        return sb.toString()
    }

    /**
     * 格式化时间戳
     */
    private fun formatTimestamp(timestamp: Long): String {
        return if (timestamp > 0) {
            dateTimeFormatter.format(Date(timestamp))
        } else {
            "未设置"
        }
    }

}
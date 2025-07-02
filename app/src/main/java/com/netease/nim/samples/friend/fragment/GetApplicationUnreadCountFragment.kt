package com.netease.nim.samples.friend.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetApplicationUnreadCountBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.friend.V2NIMFriendService
import java.text.SimpleDateFormat
import java.util.*

class GetApplicationUnreadCountFragment : BaseMethodExecuteFragment<FragmentGetApplicationUnreadCountBinding>() {

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
    ): FragmentGetApplicationUnreadCountBinding {
        return FragmentGetApplicationUnreadCountBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    /**
     * 发起请求
     */
    override fun onRequest() {

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMFriendService::class.java).getAddApplicationUnreadCount(
            { unreadCount -> updateResult(unreadCount, null) },
            { error -> updateResult(null, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(userList: Int?, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (error != null) {
            activityViewModel.refresh("获取好友申请未读数失败: $error")
            return
        }

        userList?.let {
            val resultText = buildResultText(it)
            activityViewModel.refresh(resultText)
        }
    }

    /**
     * 构建结果文本
     */
    private fun buildResultText(unreadCount:Int): String {
        val sb = StringBuilder()
        
        sb.append("获取好友申请未读数成功:\n")
        sb.append("申请列表未读数: ${unreadCount}\n")

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
package com.netease.nim.samples.friend.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetFriendListBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.friend.V2NIMFriend
import com.netease.nimlib.sdk.v2.friend.V2NIMFriendService
import java.text.SimpleDateFormat
import java.util.*

class GetFriendListFragment : BaseMethodExecuteFragment<FragmentGetFriendListBinding>() {

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
    ): FragmentGetFriendListBinding {
        return FragmentGetFriendListBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    /**
     * 发起请求
     */
    override fun onRequest() {

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMFriendService::class.java).getFriendList(
            { userList -> updateResult(userList, null) },
            { error -> updateResult(null, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(userList: List<V2NIMFriend>?, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (error != null) {
            activityViewModel.refresh("获取用户资料失败: $error")
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
    private fun buildResultText(userList: List<V2NIMFriend>): String {
        val sb = StringBuilder()
        
        sb.append("获取好友列表成功:\n")
        sb.append("好友列表数量: ${userList.size}\n")
        
        sb.append("\n好友信息详情:\n")

        userList.forEachIndexed { index, user ->
            sb.append("${index + 1}. 好友信息:\n")
            sb.append("   accountId: ${user.accountId}\n")
            sb.append("   alias: ${user.alias}\n")
            sb.append("   customerExtension: ${user.customerExtension}\n")
            sb.append("   serverExtension: ${user.serverExtension ?: "null"}\n")
            sb.append("   createTime: ${formatTimestamp(user.createTime)}\n")
            sb.append("   updateTime: ${formatTimestamp(user.updateTime)}\n")
            if (user.userProfile != null){
                sb.append(" userProfile  name: ${user.userProfile.name ?: "null"}\n")
                sb.append(" userProfile  avatar: ${user.userProfile.avatar ?: "null"}\n")
                sb.append(" userProfile  sign: ${user.userProfile.sign ?: "null"}\n")
                sb.append(" userProfile  email: ${user.userProfile.email ?: "null"}\n")
                sb.append(" userProfile  birthday: ${user.userProfile.birthday ?: "null"}\n")
                sb.append(" userProfile  mobile: ${user.userProfile.mobile ?: "null"}\n")
                sb.append(" userProfile gender: ${user.userProfile.gender}\n")
            }
            
            if (index < userList.size - 1) {
                sb.append("\n")
            }
        }

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
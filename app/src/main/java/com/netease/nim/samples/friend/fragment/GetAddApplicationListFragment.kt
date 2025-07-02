package com.netease.nim.samples.friend.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetAddApplicationListBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.friend.V2NIMFriendService
import com.netease.nimlib.sdk.v2.friend.option.V2NIMFriendAddApplicationQueryOption.V2NIMFriendAddApplicationQueryOptionBuilder
import com.netease.nimlib.sdk.v2.friend.result.V2NIMFriendAddApplicationResult
import java.text.SimpleDateFormat
import java.util.*

class GetAddApplicationListFragment : BaseMethodExecuteFragment<FragmentGetAddApplicationListBinding>() {

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
    ): FragmentGetAddApplicationListBinding {
        return FragmentGetAddApplicationListBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    /**
     * 发起请求
     */
    override fun onRequest() {
       var offstStr = binding.edtOffset.text.toString()
        var limitStr = binding.edtLimit.text.toString()
        var offset:Long = 0
        var limit:Int = 100
        try {
            offset?.let {
                offset = it.toLong()
            }

            limitStr?.let {
                limit = it.toInt()
            }
        }catch (e:NumberFormatException ){
            e.printStackTrace()
        }

        var option = V2NIMFriendAddApplicationQueryOptionBuilder.builder().withLimit(limit)
            .withOffset(offset).build()
        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMFriendService::class.java).getAddApplicationList(
            option,
            { applicationList -> updateResult(applicationList, null) },
            { error -> updateResult(null, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(resultInfo: V2NIMFriendAddApplicationResult?, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (error != null) {
            activityViewModel.refresh("获取用户资料失败: $error")
            return
        }

        resultInfo?.let {
            val resultText = buildResultText(it)
            activityViewModel.refresh(resultText)
        }
    }

    /**
     * 构建结果文本
     */
    /**
     * 构建结果文本
     */
    private fun buildResultText(resultInfo: V2NIMFriendAddApplicationResult): String {
        val sb = StringBuilder()
        var applicationiList = resultInfo.infos
        sb.append("获取好友列表成功（offset= ${resultInfo.offset}）:\n")
        sb.append("好友列表数量: ${applicationiList.size}\n")

        sb.append("\n好友信息详情:\n")

        applicationiList.forEachIndexed { index, application ->
            sb.append("${index + 1}. 好友申请信息:\n")
            sb.append("   申请者账号(applicantAccountId): ${application.applicantAccountId}\n")
            sb.append("   被申请者账号(recipientAccountId): ${application.recipientAccountId}\n")
            sb.append("   操作者账号(operatorAccountId): ${application.operatorAccountId}\n")
            sb.append("   操作时添加的附言(postscript): ${application.postscript}\n")
            sb.append("   操作的时间(timestamp): ${formatTimestamp(application.timestamp)}\n")
            sb.append("   是否已读(isRead): ${application.isRead}\n")

            if (index < applicationiList.size - 1) {
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
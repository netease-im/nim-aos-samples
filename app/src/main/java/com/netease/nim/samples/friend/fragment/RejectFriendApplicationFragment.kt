package com.netease.nim.samples.friend.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentAcceptFriendApplicationBinding
import com.netease.nim.samples.databinding.FragmentDeleteFriendApplicationBinding
import com.netease.nim.samples.databinding.FragmentRejectFriendApplicationBinding
import com.netease.nim.samples.friend.FriendApplicationSelectFragment
import com.netease.nim.samples.friend.FriendApplicationSelectViewModel
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.friend.V2NIMFriendAddApplication
import com.netease.nimlib.sdk.v2.friend.V2NIMFriendService

class RejectFriendApplicationFragment :
    BaseMethodExecuteFragment<FragmentRejectFriendApplicationBinding>() {

    /**
     * 选择会话的 ViewModel
     */
    private lateinit var selectViewModel: FriendApplicationSelectViewModel

    private var selectApplication:V2NIMFriendAddApplication?= null

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRejectFriendApplicationBinding {
        return FragmentRejectFriendApplicationBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        initConversationSelection()
    }

    private fun initClickListeners() {
        // 选择会话按钮
        binding.btnSelect.setOnClickListener {
            activityViewModel.addFragment(FriendApplicationSelectFragment.NAME)
        }
        binding.btnUnselect.setOnClickListener{
            selectApplication = null
            binding.tvSelect.text = "未选择好友申请"
        }
    }

    /**
     * 初始化会话选择功能
     */
    private fun initConversationSelection() {
        selectViewModel = getActivityViewModel(FriendApplicationSelectViewModel::class.java)
        selectViewModel.selectItemLiveData.observe(viewLifecycleOwner) { item ->
            // 从选择的会话中填入会话ID
            activityViewModel.popFragment()
            selectApplication = item
            val sb = StringBuilder()
            sb.append("好友申请详情:\n")
            sb.append("• 好友申请者ID: ${item.applicantAccountId}\n")
            sb.append("• 被申请者ID: ${item.recipientAccountId}\n")
            sb.append("• 操作者ID: ${item.operatorAccountId}\n")
            sb.append("• 操作时添加的附言: ${item.postscript}\n")
            binding.tvSelect.text = sb.toString()
            showToast("已选择好友申请: ${item.applicantAccountId ?: item.operatorAccountId}")
        }
    }

    /**
     * 发起请求
     */
    override fun onRequest() {

        if (selectApplication == null) {
            showToast("选择好友申请")
            return
        }

        // 显示确认对话框
        showDeleteConfirmation(selectApplication!!.applicantAccountId)
    }

    /**
     * 显示删除确认对话框
     */
    private fun showDeleteConfirmation(targetId: String) {
        val messageText = "确定要拒绝好友申请吗？\n\n申请者账号: $targetId\n\n消息将被保留"

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("确认拒绝")
            .setMessage(messageText)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("确定") { _, _ ->
                performDelete(targetId)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 执行删除操作
     */
    private fun performDelete(targetId: String) {
        activityViewModel.showLoadingDialog()
        if (selectApplication != null) {
            NIMClient.getService(V2NIMFriendService::class.java).rejectAddApplication(
                selectApplication,"",
                { updateResult(true, selectApplication!!, null) },
                { error -> updateResult(false, selectApplication!!, error) }
            )
        }
    }

    /**
     * 更新结果
     */
    private fun updateResult(success: Boolean,application:V2NIMFriendAddApplication, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (success) {
            val resultText = buildSuccessResultText(application)
            activityViewModel.refresh(resultText)
            
            // 删除成功后清空输入框
            binding.tvSelect.setText("未选择好友申请")
        } else {
            val resultText = buildFailureResultText(application, error)
            activityViewModel.refresh(resultText)
        }
    }

    /**
     * 构建成功结果文本
     */
    private fun buildSuccessResultText(application:V2NIMFriendAddApplication): String {
        val sb = StringBuilder()
        
        sb.append("拒绝好友申请成功! ✅\n\n")
        sb.append("操作详情:\n")
        sb.append("• 好友申请者ID: ${application.applicantAccountId}\n")
        sb.append("• 被申请者ID: ${application.recipientAccountId}\n")
        sb.append("• 操作者ID: ${application.operatorAccountId}\n")
        sb.append("• 操作时添加的附言: ${application.postscript}\n")

        return sb.toString()
    }
     /**
     * 构建失败结果文本
     */   private fun buildFailureResultText( application:V2NIMFriendAddApplication, error: V2NIMError?): String {
         val sb = StringBuilder()
         if (error != null) {
            sb.append("error信息："+error.desc)
         }
         sb.append("拒绝好友申请失败 \n\n")
         sb.append("操作详情:\n")
         sb.append("• 好友申请者ID: ${application.applicantAccountId}\n")
         sb.append("• 被申请者ID: ${application.recipientAccountId}\n")
        
        return sb.toString()
    }

}
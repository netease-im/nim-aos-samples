package com.netease.nim.samples.user.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentRemoveUserFromBlockListBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.user.V2NIMUserService

class RemoveUserFromBlockListFragment : BaseMethodExecuteFragment<FragmentRemoveUserFromBlockListBinding>() {

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRemoveUserFromBlockListBinding {
        return FragmentRemoveUserFromBlockListBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        initTextWatcher()
        updateValidationStatus()
    }

    private fun initClickListeners() {
        // 清空账号按钮
        binding.btnClearAccount.setOnClickListener {
            binding.edtAccountId.setText("")
            showToast("已清空用户账号")
        }

        // 填入测试账号按钮
        binding.btnAddTestAccount.setOnClickListener {
            addTestAccount()
        }
    }

    private fun initTextWatcher() {
        binding.edtAccountId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updateUIStatus()
            }
        })
    }

    /**
     * 填入测试账号
     */
    private fun addTestAccount() {
        val testAccount = "unblock_test_user"
        binding.edtAccountId.setText(testAccount)
        showToast("已填入测试账号: $testAccount")
    }

    /**
     * 更新UI状态
     */
    private fun updateUIStatus() {
        val accountId = getAccountId()
        
        // 更新账号状态显示
        updateAccountStatus(accountId)
        
        // 更新操作预览
        updateOperationPreview()
        
        // 更新验证状态
        updateValidationStatus()
    }

    /**
     * 更新账号状态显示
     */
    private fun updateAccountStatus(accountId: String) {
        binding.tvAccountStatus.text = if (accountId.isEmpty()) {
            "账号状态: 未输入"
        } else {
            "账号状态: 准备从黑名单移除 \"$accountId\""
        }
    }

    /**
     * 更新操作预览
     */
    private fun updateOperationPreview() {
        val accountId = getAccountId()
        
        if (accountId.isEmpty()) {
            binding.tvOperationPreview.text = "暂无操作"
            return
        }

        val preview = "✅ 从黑名单移除用户 \"$accountId\"\n" +
                "• 该用户将恢复向你发送消息的权限\n" +
                "• 你可以正常收到该用户的消息\n" +
                "• 移除设置立即生效"
        
        binding.tvOperationPreview.text = preview
    }

    /**
     * 更新验证状态
     */
    private fun updateValidationStatus() {
        val accountId = getAccountId()
        
        when {
            accountId.isEmpty() -> {
                binding.tvValidationStatus.text = "✗ 用户账号不能为空"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
            !isValidAccountId(accountId) -> {
                binding.tvValidationStatus.text = "✗ 用户账号格式不正确"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
            else -> {
                binding.tvValidationStatus.text = "✓ 参数验证通过，可以从黑名单移除"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            }
        }
    }

    /**
     * 验证账号ID格式
     */
    private fun isValidAccountId(accountId: String): Boolean {
        // 简单的账号格式验证：不能包含特殊字符，长度合理
        return accountId.matches(Regex("^[a-zA-Z0-9_-]+$")) && accountId.length in 1..64
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        val accountId = getAccountId()
        
        if (accountId.isEmpty()) {
            showToast("请输入用户账号")
            return
        }

        if (!isValidAccountId(accountId)) {
            showToast("用户账号格式不正确")
            return
        }

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMUserService::class.java).removeUserFromBlockList(
            accountId,
            { updateResult(true, null) },
            { error -> updateResult(false, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(success: Boolean, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (!success && error != null) {
            activityViewModel.refresh("从黑名单移除用户失败: $error")
            return
        }

        val resultText = buildResultText()
        activityViewModel.refresh(resultText)
    }

    /**
     * 构建结果文本
     */
    private fun buildResultText(): String {
        val accountId = getAccountId()
        val sb = StringBuilder()
        
        sb.append("从黑名单移除用户成功:\n\n")
        
        sb.append("✅ 操作详情:\n")
        sb.append("• 目标用户: $accountId\n")
        sb.append("• 操作类型: 从黑名单移除\n")
        sb.append("• 执行结果: 成功\n")
        sb.append("• 生效时间: 立即生效\n\n")
        
        sb.append("📋 移除效果:\n")
        sb.append("• 该用户恢复向你发送消息的权限\n")
        sb.append("• 你可以正常收到该用户的消息\n")
        sb.append("• 包括文本、图片、语音等所有类型消息\n")
        sb.append("• 双方恢复正常的消息通信\n\n")
        
        sb.append("🔧 后续操作:\n")
        sb.append("• 可以随时重新添加该用户到黑名单\n")
        sb.append("• 可以查看当前的黑名单列表\n")
        sb.append("• 移除不在黑名单中的用户不会报错\n")
        sb.append("• 黑名单设置会同步到其他设备\n\n")
        
        sb.append("💡 温馨提示:\n")
        sb.append("• 移除黑名单后该用户可以立即发送消息\n")
        sb.append("• 建议确认该用户不再发送骚扰消息\n")
        sb.append("• 如有必要可随时重新添加到黑名单")

        return sb.toString()
    }

    /**
     * 获取用户账号
     */
    private fun getAccountId(): String {
        return binding.edtAccountId.text.toString().trim()
    }
}
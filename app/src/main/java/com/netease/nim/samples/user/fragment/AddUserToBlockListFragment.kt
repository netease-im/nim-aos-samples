package com.netease.nim.samples.user.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentAddUserToBlockListBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.user.V2NIMUserService

class AddUserToBlockListFragment : BaseMethodExecuteFragment<FragmentAddUserToBlockListBinding>() {

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAddUserToBlockListBinding {
        return FragmentAddUserToBlockListBinding.inflate(inflater, container, false)
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
        val testAccount = "block_test_user"
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
            "账号状态: 准备添加 \"$accountId\" 到黑名单"
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

        val preview = "🚫 将用户 \"$accountId\" 添加到黑名单\n" +
                "• 该用户将无法向你发送消息\n" +
                "• 你也收不到该用户的任何消息\n" +
                "• 黑名单设置立即生效"
        
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
                binding.tvValidationStatus.text = "✓ 参数验证通过，可以添加到黑名单"
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
        NIMClient.getService(V2NIMUserService::class.java).addUserToBlockList(
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
            activityViewModel.refresh("添加用户到黑名单失败: $error")
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
        
        sb.append("添加用户到黑名单成功:\n\n")
        
        sb.append("🚫 操作详情:\n")
        sb.append("• 目标用户: $accountId\n")
        sb.append("• 操作类型: 添加到黑名单\n")
        sb.append("• 执行结果: 成功\n")
        sb.append("• 生效时间: 立即生效\n\n")
        
        sb.append("📋 黑名单效果:\n")
        sb.append("• 该用户无法再向你发送消息\n")
        sb.append("• 你也无法收到该用户的任何消息\n")
        sb.append("• 包括文本、图片、语音等所有类型消息\n")
        sb.append("• 群聊中该用户的消息仍然可见\n\n")
        
        sb.append("🔧 后续操作:\n")
        sb.append("• 可以随时从黑名单移除该用户\n")
        sb.append("• 可以查看完整的黑名单列表\n")
        sb.append("• 重复添加同一用户不会报错\n")
        sb.append("• 黑名单设置会同步到其他设备\n\n")
        
        sb.append("💡 温馨提示:\n")
        sb.append("• 黑名单功能是为了保护用户免受骚扰\n")
        sb.append("• 建议谨慎使用，避免误操作\n")
        sb.append("• 如有疑问可随时移除黑名单设置")

        return sb.toString()
    }

    /**
     * 获取用户账号
     */
    private fun getAccountId(): String {
        return binding.edtAccountId.text.toString().trim()
    }
}
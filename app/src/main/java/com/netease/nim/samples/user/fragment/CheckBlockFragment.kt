package com.netease.nim.samples.user.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentCheckBlockBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.user.V2NIMUserService
import java.text.SimpleDateFormat
import java.util.*

class CheckBlockFragment : BaseMethodExecuteFragment<FragmentCheckBlockBinding>() {

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
    ): FragmentCheckBlockBinding {
        return FragmentCheckBlockBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        initTextWatcher()
        updateValidationStatus()
    }

    private fun initClickListeners() {
        // 清空按钮
        binding.btnClearIds.setOnClickListener {
            binding.edtAccountIds.setText("")
            showToast("已清空用户账号列表")
        }

        // 添加测试账号按钮
        binding.btnAddTestIds.setOnClickListener {
            addTestAccountIds()
        }

        // 添加混合状态账号按钮
        binding.btnAddMixedIds.setOnClickListener {
            addMixedStatusAccountIds()
        }
    }

    private fun initTextWatcher() {
        binding.edtAccountIds.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updateUIStatus()
            }
        })
    }

    /**
     * 添加测试账号
     */
    private fun addTestAccountIds() {
        val testAccountIds = listOf("check001", "check002", "check003", "blockuser123", "normal456")
        val currentText = binding.edtAccountIds.text.toString().trim()

        val newIds = if (currentText.isEmpty()) {
            testAccountIds.joinToString(",")
        } else {
            "$currentText,${testAccountIds.joinToString(",")}"
        }

        binding.edtAccountIds.setText(newIds)
        showToast("已添加 ${testAccountIds.size} 个测试账号")
    }

    /**
     * 添加混合状态账号（包含可能在黑名单和不在黑名单的账号）
     */
    private fun addMixedStatusAccountIds() {
        val mixedAccountIds = listOf(
            "blocked_user1",    // 可能在黑名单
            "normal_user1",     // 正常用户
            "spam_account",     // 垃圾账号
            "invalid@user",     // 无效格式
            "nonexistent999"    // 不存在的账号
        )
        val currentText = binding.edtAccountIds.text.toString().trim()

        val newIds = if (currentText.isEmpty()) {
            mixedAccountIds.joinToString(",")
        } else {
            "$currentText,${mixedAccountIds.joinToString(",")}"
        }

        binding.edtAccountIds.setText(newIds)
        showToast("已添加 ${mixedAccountIds.size} 个混合状态账号")
    }

    /**
     * 更新UI状态
     */
    private fun updateUIStatus() {
        val accountIds = getAccountIdList()

        // 更新账号数量显示
        binding.tvIdsCount.text = "当前输入账号数量: ${accountIds.size}个"

        // 更新账号预览
        updateIdsPreview(accountIds)

        // 更新黑名单状态预览
        updateBlockStatusPreview(accountIds)

        // 更新验证状态
        updateValidationStatus()
    }

    /**
     * 更新账号预览
     */
    private fun updateIdsPreview(accountIds: List<String>) {
        if (accountIds.isEmpty()) {
            binding.tvIdsPreview.text = "暂无账号"
            return
        }

        val previewText = if (accountIds.size <= 5) {
            accountIds.joinToString("\n")
        } else {
            accountIds.take(5).joinToString("\n") + "\n... 还有${accountIds.size - 5}个账号"
        }

        binding.tvIdsPreview.text = previewText
    }

    /**
     * 更新黑名单状态预览
     */
    private fun updateBlockStatusPreview(accountIds: List<String>) {
        if (accountIds.isEmpty()) {
            binding.tvBlockStatusPreview.text = "暂未检查黑名单状态"
            return
        }

        val previewText = StringBuilder()
        previewText.append("🔍 将要检查的账号黑名单状态:\n")

        accountIds.take(8).forEachIndexed { index, accountId ->
            previewText.append("${index + 1}. $accountId → 待检查\n")
        }

        if (accountIds.size > 8) {
            previewText.append("... 还有${accountIds.size - 8}个账号待检查")
        }

        binding.tvBlockStatusPreview.text = previewText.toString().trimEnd()
    }

    /**
     * 更新验证状态
     */
    private fun updateValidationStatus() {
        val accountIds = getAccountIdList()

        when {
            accountIds.isEmpty() -> {
                binding.tvValidationStatus.text = "✗ 用户账号列表不能为空"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
            else -> {
                val duplicates = accountIds.groupingBy { it }.eachCount().filter { it.value > 1 }
                if (duplicates.isNotEmpty()) {
                    binding.tvValidationStatus.text = "⚠ 检测到重复账号: ${duplicates.keys.joinToString(", ")}"
                    binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
                } else {
                    binding.tvValidationStatus.text = "✓ 参数验证通过，共${accountIds.size}个有效账号"
                    binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                }
            }
        }
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        val accountIds = getAccountIdList()

        if (accountIds.isEmpty()) {
            showToast("请输入用户账号列表")
            return
        }

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMUserService::class.java).checkBlock(
            accountIds,
            { blockStatusMap -> updateResult(blockStatusMap, null) },
            { error -> updateResult(null, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(blockStatusMap: Map<String, Boolean>?, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()

        if (error != null) {
            activityViewModel.refresh("检查黑名单状态失败: $error")
            return
        }

        blockStatusMap?.let {
            // 更新黑名单状态预览显示
            updateBlockStatusPreviewWithResult(it)

            val resultText = buildResultText(it)
            activityViewModel.refresh(resultText)
        }
    }

    /**
     * 使用检查结果更新黑名单状态预览
     */
    private fun updateBlockStatusPreviewWithResult(blockStatusMap: Map<String, Boolean>) {
        val previewText = StringBuilder()
        previewText.append("✅ 黑名单状态检查结果:\n")

        val sortedEntries = blockStatusMap.entries.sortedBy { !it.value } // 黑名单用户排在前面

        sortedEntries.take(8).forEach { (accountId, isBlocked) ->
            val status = if (isBlocked) "🚫 在黑名单" else "✅ 不在黑名单"
            previewText.append("• $accountId → $status\n")
        }

        if (blockStatusMap.size > 8) {
            previewText.append("... 还有${blockStatusMap.size - 8}个结果")
        }

        binding.tvBlockStatusPreview.text = previewText.toString().trimEnd()
    }

    /**
     * 构建结果文本
     */
    private fun buildResultText(blockStatusMap: Map<String, Boolean>): String {
        val inputIds = getAccountIdList()
        val sb = StringBuilder()

        sb.append("检查黑名单状态成功:\n\n")

        // 基本统计
        val blockedUsers = blockStatusMap.filter { it.value }
        val nonBlockedUsers = blockStatusMap.filter { !it.value }

        sb.append("📊 检查统计:\n")
        sb.append("• 输入账号数量: ${inputIds.size}\n")
        sb.append("• 返回结果数量: ${blockStatusMap.size}\n")
        sb.append("• 在黑名单用户: ${blockedUsers.size}个\n")
        sb.append("• 不在黑名单用户: ${nonBlockedUsers.size}个\n")
        sb.append("• 检查时间: ${dateTimeFormatter.format(Date())}\n\n")

        // 黑名单用户详情
        if (blockedUsers.isNotEmpty()) {
            sb.append("🚫 在黑名单中的用户:\n")
            blockedUsers.entries.forEachIndexed { index, (accountId, _) ->
                sb.append("${index + 1}. $accountId\n")
            }
            sb.append("\n")
        }

        // 非黑名单用户详情
        if (nonBlockedUsers.isNotEmpty()) {
            sb.append("✅ 不在黑名单中的用户:\n")
            nonBlockedUsers.entries.forEachIndexed { index, (accountId, _) ->
                sb.append("${index + 1}. $accountId\n")
            }
            sb.append("\n")
        }

        // 详细结果映射
        sb.append("📋 详细结果映射:\n")
        val sortedResults = blockStatusMap.entries.sortedBy { !it.value } // 黑名单用户排在前面
        sortedResults.forEachIndexed { index, (accountId, isBlocked) ->
            val status = if (isBlocked) "true (在黑名单)" else "false (不在黑名单)"
            sb.append("${index + 1}. \"$accountId\" → $status\n")
        }

        sb.append("\n💡 结果说明:\n")
        sb.append("• true: 用户在黑名单中，已被屏蔽\n")
        sb.append("• false: 用户不在黑名单/不存在/账号格式错误\n")
        sb.append("• checkBlock接口对所有非黑名单情况统一返回false\n")
        sb.append("• 可以使用此接口批量检查用户屏蔽状态\n\n")

        sb.append("🔧 后续操作建议:\n")
        if (blockedUsers.isNotEmpty()) {
            sb.append("• 发现${blockedUsers.size}个被屏蔽用户\n")
            sb.append("• 可以使用移除黑名单功能解除屏蔽\n")
            sb.append("• 黑名单用户无法发送消息给你\n")
        }
        if (nonBlockedUsers.isNotEmpty()) {
            sb.append("• 发现${nonBlockedUsers.size}个正常用户\n")
            sb.append("• 这些用户可以正常发送消息\n")
            sb.append("• 如需屏蔽可使用添加黑名单功能\n")
        }

        return sb.toString()
    }

    /**
     * 获取用户账号列表
     */
    private fun getAccountIdList(): List<String> {
        val idsText = binding.edtAccountIds.text.toString().trim()
        if (idsText.isEmpty()) {
            return emptyList()
        }

        // 支持逗号分隔
        return idsText.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct() // 去重
    }
}
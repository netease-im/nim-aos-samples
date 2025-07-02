package com.netease.nim.samples.user.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetUserListFromCloudBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.user.V2NIMUser
import com.netease.nimlib.sdk.v2.user.V2NIMUserService
import java.text.SimpleDateFormat
import java.util.*

class GetUserListFromCloudFragment : BaseMethodExecuteFragment<FragmentGetUserListFromCloudBinding>() {

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
    ): FragmentGetUserListFromCloudBinding {
        return FragmentGetUserListFromCloudBinding.inflate(inflater, container, false)
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
        val testAccountIds = listOf("test001", "test002", "test003", "user123", "demo456")
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
     * 更新UI状态
     */
    private fun updateUIStatus() {
        val accountIds = getAccountIdList()
        
        // 更新账号数量显示
        binding.tvIdsCount.text = "当前输入账号数量: ${accountIds.size}个"
        
        // 更新账号预览
        updateIdsPreview(accountIds)
        
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
     * 更新验证状态
     */
    private fun updateValidationStatus() {
        val accountIds = getAccountIdList()
        
        when {
            accountIds.isEmpty() -> {
                binding.tvValidationStatus.text = "✗ 用户账号列表不能为空"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
            accountIds.size > 150 -> {
                binding.tvValidationStatus.text = "✗ 账号数量超过限制，最多150个（当前${accountIds.size}个）"
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

        if (accountIds.size > 150) {
            showToast("账号数量不能超过150个")
            return
        }

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMUserService::class.java).getUserListFromCloud(
            accountIds,
            { userList -> updateResult(userList, null) },
            { error -> updateResult(null, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(userList: List<V2NIMUser>?, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (error != null) {
            activityViewModel.refresh("从云端获取用户资料失败: $error")
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
    private fun buildResultText(userList: List<V2NIMUser>): String {
        val inputIds = getAccountIdList()
        val sb = StringBuilder()
        
        sb.append("从云端获取用户资料成功:\n")
        sb.append("输入账号数量: ${inputIds.size}\n")
        sb.append("获取到用户数量: ${userList.size}\n")
        
        if (userList.size < inputIds.size) {
            val foundIds = userList.map { it.accountId }.toSet()
            val notFoundIds = inputIds.filter { it !in foundIds }
            sb.append("未找到的账号: ${notFoundIds.joinToString(", ")}\n")
        }
        
        sb.append("\n🌐 云端查询特点:\n")
        sb.append("• 直接从服务器获取最新数据\n")
        sb.append("• 跳过本地缓存，确保数据实时性\n")
        sb.append("• 查询后自动更新本地缓存\n")
        sb.append("• 可能触发用户信息更新回调\n")
        
        sb.append("\n用户资料详情:\n")

        userList.forEachIndexed { index, user ->
            sb.append("${index + 1}. 用户信息:\n")
            sb.append("   accountId: ${user.accountId}\n")
            sb.append("   name: ${user.name ?: "null"}\n")
            sb.append("   avatar: ${user.avatar ?: "null"}\n")
            sb.append("   sign: ${user.sign ?: "null"}\n")
            sb.append("   email: ${user.email ?: "null"}\n")
            sb.append("   birthday: ${user.birthday ?: "null"}\n")
            sb.append("   mobile: ${user.mobile ?: "null"}\n")
            sb.append("   gender: ${user.gender}\n")
            sb.append("   serverExtension: ${user.serverExtension ?: "null"}\n")
            sb.append("   createTime: ${formatTimestamp(user.createTime)}\n")
            sb.append("   updateTime: ${formatTimestamp(user.updateTime)}\n")
            
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
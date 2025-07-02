package com.netease.nim.samples.friend.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetFriendListIdsBinding
import com.netease.nim.samples.databinding.FragmentGetUserListBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.friend.V2NIMFriend
import com.netease.nimlib.sdk.v2.friend.V2NIMFriendService
import com.netease.nimlib.sdk.v2.user.V2NIMUser
import com.netease.nimlib.sdk.v2.user.V2NIMUserService
import java.text.SimpleDateFormat
import java.util.*

class GetFriendListByIdsFragment : BaseMethodExecuteFragment<FragmentGetFriendListIdsBinding>() {

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
    ): FragmentGetFriendListIdsBinding {
        return FragmentGetFriendListIdsBinding.inflate(inflater, container, false)
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
        NIMClient.getService(V2NIMFriendService::class.java).getFriendList(
            { userList ->
                if (userList.size > 0) {
                    val testAccountIds = listOf(userList.get(0).accountId)
                    val currentText = binding.edtAccountIds.text.toString().trim()
                    val newIds = if (currentText.isEmpty()) {
                        testAccountIds.joinToString(",")
                    } else {
                        "$currentText,${testAccountIds.joinToString(",")}"
                    }

                    binding.edtAccountIds.setText(newIds)
                    showToast("已添加 ${testAccountIds.size} 个测试账号")
                }
            },
            { error -> showToast("获取账号") }
        )

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
        NIMClient.getService(V2NIMFriendService::class.java).getFriendByIds(
            accountIds,
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
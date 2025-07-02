package com.netease.nim.samples.user.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentSearchUserByOptionBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.user.V2NIMUser
import com.netease.nimlib.sdk.v2.user.V2NIMUserService
import com.netease.nimlib.sdk.v2.user.option.V2NIMUserSearchOption
import java.text.SimpleDateFormat
import java.util.*

class SearchUserByOptionFragment : BaseMethodExecuteFragment<FragmentSearchUserByOptionBinding>() {

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
    ): FragmentSearchUserByOptionBinding {
        return FragmentSearchUserByOptionBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        initTextWatcher()
        initCheckBoxListeners()
        updateValidationStatus()
        updateSearchOptionPreview()
    }

    private fun initClickListeners() {
        // 清空关键字按钮
        binding.btnClearKeyword.setOnClickListener {
            binding.edtSearchKeyword.setText("")
            showToast("已清空搜索关键字")
        }

        // 测试昵称搜索按钮
        binding.btnTestNameSearch.setOnClickListener {
            testNameSearch()
        }

        // 测试账号搜索按钮
        binding.btnTestAccountSearch.setOnClickListener {
            testAccountSearch()
        }

        // 测试手机搜索按钮
        binding.btnTestMobileSearch.setOnClickListener {
            testMobileSearch()
        }

        // 重置范围按钮
        binding.btnResetScope.setOnClickListener {
            resetSearchScope()
        }
    }

    private fun initTextWatcher() {
        binding.edtSearchKeyword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updateUIStatus()
            }
        })
    }

    private fun initCheckBoxListeners() {
        val checkBoxListener = CompoundButton.OnCheckedChangeListener { _, _ ->
            updateUIStatus()
        }

        binding.cbSearchName.setOnCheckedChangeListener(checkBoxListener)
        binding.cbSearchAccountId.setOnCheckedChangeListener(checkBoxListener)
        binding.cbSearchMobile.setOnCheckedChangeListener(checkBoxListener)
    }

    /**
     * 测试昵称搜索
     */
    private fun testNameSearch() {
        binding.edtSearchKeyword.setText("测试用户")
        binding.cbSearchName.isChecked = true
        binding.cbSearchAccountId.isChecked = false
        binding.cbSearchMobile.isChecked = false
        showToast("已设置昵称搜索测试")
    }

    /**
     * 测试账号搜索
     */
    private fun testAccountSearch() {
        binding.edtSearchKeyword.setText("test001")
        binding.cbSearchName.isChecked = false
        binding.cbSearchAccountId.isChecked = true
        binding.cbSearchMobile.isChecked = false
        showToast("已设置账号搜索测试")
    }

    /**
     * 测试手机搜索
     */
    private fun testMobileSearch() {
        binding.edtSearchKeyword.setText("138")
        binding.cbSearchName.isChecked = false
        binding.cbSearchAccountId.isChecked = false
        binding.cbSearchMobile.isChecked = true
        showToast("已设置手机号搜索测试")
    }

    /**
     * 重置搜索范围
     */
    private fun resetSearchScope() {
        binding.cbSearchName.isChecked = true
        binding.cbSearchAccountId.isChecked = false
        binding.cbSearchMobile.isChecked = false
        showToast("已重置为默认搜索范围")
    }

    /**
     * 更新UI状态
     */
    private fun updateUIStatus() {
        val keyword = getSearchKeyword()
        
        // 更新关键字长度显示
        binding.tvKeywordLength.text = "关键字长度: ${keyword.length}个字符"
        
        // 更新搜索选项预览
        updateSearchOptionPreview()
        
        // 更新验证状态
        updateValidationStatus()
    }

    /**
     * 更新搜索选项预览
     */
    private fun updateSearchOptionPreview() {
        val keyword = getSearchKeyword()
        val searchScopes = getSearchScopes()
        
        val previewText = StringBuilder()
        previewText.append("🔍 搜索关键字: ${if (keyword.isEmpty()) "空" else "\"$keyword\""}\n")
        previewText.append("📊 关键字长度: ${keyword.length}个字符\n")
        previewText.append("🎯 搜索范围: ${searchScopes.ifEmpty { "未选择" }}\n")
        
        if (searchScopes.isNotEmpty()) {
            previewText.append("🔄 搜索逻辑: ${searchScopes.joinToString(" 或 ")}\n")
        }
        
        previewText.append("⏰ 预览时间: ${dateTimeFormatter.format(Date())}")
        
        binding.tvSearchOptionPreview.text = previewText.toString()
    }

    /**
     * 更新搜索结果预览
     */
    private fun updateSearchResultPreview(userList: List<V2NIMUser>?) {
        if (userList == null) {
            binding.tvSearchResultPreview.text = "暂未执行搜索"
            return
        }

        val previewText = StringBuilder()
        previewText.append("✅ 搜索完成，找到 ${userList.size} 个用户:\n")
        
        if (userList.isEmpty()) {
            previewText.append("🔍 未找到匹配的用户\n")
            previewText.append("💡 建议：调整搜索关键字或扩大搜索范围")
        } else {
            userList.take(8).forEachIndexed { index, user ->
                previewText.append("${index + 1}. ${user.name ?: user.accountId}\n")
            }
            
            if (userList.size > 8) {
                previewText.append("... 还有${userList.size - 8}个用户")
            }
        }
        
        binding.tvSearchResultPreview.text = previewText.toString().trimEnd()
    }

    /**
     * 获取搜索范围描述
     */
    private fun getSearchScopes(): List<String> {
        val scopes = mutableListOf<String>()
        
        if (binding.cbSearchName.isChecked) {
            scopes.add("昵称")
        }
        if (binding.cbSearchAccountId.isChecked) {
            scopes.add("账号ID")
        }
        if (binding.cbSearchMobile.isChecked) {
            scopes.add("手机号")
        }
        
        return scopes
    }

    /**
     * 更新验证状态
     */
    private fun updateValidationStatus() {
        val keyword = getSearchKeyword()
        val hasSearchScope = binding.cbSearchName.isChecked || 
                           binding.cbSearchAccountId.isChecked || 
                           binding.cbSearchMobile.isChecked
        
        when {
            keyword.isEmpty() -> {
                binding.tvValidationStatus.text = "✗ 搜索关键字不能为空"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
            !hasSearchScope -> {
                binding.tvValidationStatus.text = "✗ 必须至少选择一个搜索范围"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
            keyword.length < 2 -> {
                binding.tvValidationStatus.text = "⚠ 建议关键字长度至少2个字符，提高搜索准确性"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
            }
            else -> {
                val scopes = getSearchScopes()
                binding.tvValidationStatus.text = "✓ 参数验证通过，将在 ${scopes.joinToString("、")} 中搜索"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            }
        }
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        val keyword = getSearchKeyword()
        
        if (keyword.isEmpty()) {
            showToast("请输入搜索关键字")
            return
        }

        val hasSearchScope = binding.cbSearchName.isChecked || 
                           binding.cbSearchAccountId.isChecked || 
                           binding.cbSearchMobile.isChecked
        
        if (!hasSearchScope) {
            showToast("请至少选择一个搜索范围")
            return
        }

        val searchOption = createUserSearchOption()
        
        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMUserService::class.java).searchUserByOption(
            searchOption,
            { userList -> updateResult(userList, null) },
            { error -> updateResult(null, error) }
        )
    }

    /**
     * 创建用户搜索选项
     */
    private fun createUserSearchOption(): V2NIMUserSearchOption {
        return V2NIMUserSearchOption.V2NIMUserSearchOptionBuilder
            .builder(getSearchKeyword())
            .withSearchName(binding.cbSearchName.isChecked)
            .withSearchAccountId(binding.cbSearchAccountId.isChecked)
            .withSearchMobile(binding.cbSearchMobile.isChecked)
            .build()
    }

    /**
     * 更新结果
     */
    private fun updateResult(userList: List<V2NIMUser>?, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (error != null) {
            activityViewModel.refresh("搜索用户失败: $error")
            return
        }

        userList?.let {
            // 更新搜索结果预览显示
            updateSearchResultPreview(it)
            
            val resultText = buildResultText(it)
            activityViewModel.refresh(resultText)
        }
    }

    /**
     * 构建结果文本
     */
    private fun buildResultText(userList: List<V2NIMUser>): String {
        val keyword = getSearchKeyword()
        val scopes = getSearchScopes()
        val sb = StringBuilder()
        
        sb.append("搜索用户成功:\n\n")
        
        // 搜索条件信息
        sb.append("🔍 搜索条件:\n")
        sb.append("• 关键字: \"$keyword\"\n")
        sb.append("• 搜索范围: ${scopes.joinToString("、")}\n")
        sb.append("• 搜索逻辑: ${scopes.joinToString(" 或 ")}\n")
        sb.append("• 搜索时间: ${dateTimeFormatter.format(Date())}\n\n")
        
        // 搜索统计
        sb.append("📊 搜索结果统计:\n")
        sb.append("• 找到用户数量: ${userList.size}个\n")
        
        if (userList.isEmpty()) {
            sb.append("• 搜索状态: 无匹配结果\n\n")
            
            sb.append("💡 搜索建议:\n")
            sb.append("• 尝试使用更通用的关键字\n")
            sb.append("• 检查关键字拼写是否正确\n")
            sb.append("• 扩大搜索范围（选择更多搜索字段）\n")
            sb.append("• 使用部分关键字进行模糊搜索\n")
            sb.append("• 确认要搜索的用户确实存在")
        } else {
            sb.append("• 搜索状态: 找到匹配用户\n")
            
            // 按搜索字段分类统计
            val nameMatches = userList.filter { user ->
                binding.cbSearchName.isChecked && 
                user.name?.contains(keyword, ignoreCase = true) == true
            }
            val accountMatches = userList.filter { user ->
                binding.cbSearchAccountId.isChecked && 
                user.accountId.contains(keyword, ignoreCase = true)
            }
            val mobileMatches = userList.filter { user ->
                binding.cbSearchMobile.isChecked && 
                user.mobile?.contains(keyword, ignoreCase = true) == true
            }
            
            if (nameMatches.isNotEmpty()) {
                sb.append("• 昵称匹配: ${nameMatches.size}个\n")
            }
            if (accountMatches.isNotEmpty()) {
                sb.append("• 账号匹配: ${accountMatches.size}个\n")
            }
            if (mobileMatches.isNotEmpty()) {
                sb.append("• 手机匹配: ${mobileMatches.size}个\n")
            }
            
            sb.append("\n👥 搜索结果详情:\n")

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
                
                // 显示匹配字段
                val matchedFields = mutableListOf<String>()
                if (binding.cbSearchName.isChecked && user.name?.contains(keyword, ignoreCase = true) == true) {
                    matchedFields.add("昵称")
                }
                if (binding.cbSearchAccountId.isChecked && user.accountId.contains(keyword, ignoreCase = true)) {
                    matchedFields.add("账号ID")
                }
                if (binding.cbSearchMobile.isChecked && user.mobile?.contains(keyword, ignoreCase = true) == true) {
                    matchedFields.add("手机号")
                }
                
                if (matchedFields.isNotEmpty()) {
                    sb.append("   匹配字段: ${matchedFields.joinToString("、")}\n")
                }
                
                if (index < userList.size - 1) {
                    sb.append("\n")
                }
            }
            
            sb.append("\n\n🔧 后续操作建议:\n")
            sb.append("• 可以根据搜索结果获取更详细的用户信息\n")
            sb.append("• 支持将搜索到的用户添加为好友\n")
            sb.append("• 可以进一步查看用户的详细资料\n")
            sb.append("• 如需精确匹配，可调整搜索关键字")
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
     * 获取搜索关键字
     */
    private fun getSearchKeyword(): String {
        return binding.edtSearchKeyword.text.toString().trim()
    }
}
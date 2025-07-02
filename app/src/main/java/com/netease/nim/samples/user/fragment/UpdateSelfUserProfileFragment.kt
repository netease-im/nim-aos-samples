package com.netease.nim.samples.user.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentUpdateSelfUserProfileBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.user.V2NIMUserService
import com.netease.nimlib.sdk.v2.user.params.V2NIMUserUpdateParams

class UpdateSelfUserProfileFragment : BaseMethodExecuteFragment<FragmentUpdateSelfUserProfileBinding>() {

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUpdateSelfUserProfileBinding {
        return FragmentUpdateSelfUserProfileBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        initTextWatchers()
        initRadioGroupListener()
        updateValidationStatus()
    }

    private fun initClickListeners() {
        // 清空所有按钮
        binding.btnClearAll.setOnClickListener {
            clearAllFields()
            showToast("已清空所有输入")
        }

        // 填入示例按钮
        binding.btnFillSample.setOnClickListener {
            fillSampleData()
            showToast("已填入示例数据")
        }
    }

    private fun initTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateUIStatus()
            }
        }

        binding.edtName.addTextChangedListener(textWatcher)
        binding.edtAvatar.addTextChangedListener(textWatcher)
        binding.edtSign.addTextChangedListener(textWatcher)
        binding.edtEmail.addTextChangedListener(textWatcher)
        binding.edtMobile.addTextChangedListener(textWatcher)
        binding.edtBirthday.addTextChangedListener(textWatcher)
        binding.edtServerExtension.addTextChangedListener(textWatcher)
    }

    private fun initRadioGroupListener() {
        binding.rgGender.setOnCheckedChangeListener { _, _ ->
            updateUIStatus()
        }
    }

    /**
     * 清空所有输入字段
     */
    private fun clearAllFields() {
        binding.edtName.setText("")
        binding.edtAvatar.setText("")
        binding.edtSign.setText("")
        binding.edtEmail.setText("")
        binding.edtMobile.setText("")
        binding.edtBirthday.setText("")
        binding.edtServerExtension.setText("")
        binding.rbGenderUnset.isChecked = true
    }

    /**
     * 填入示例数据
     */
    private fun fillSampleData() {
        binding.edtName.setText("示例用户")
        binding.edtAvatar.setText("https://example.com/avatar.jpg")
        binding.edtSign.setText("这是一个示例个性签名")
        binding.edtEmail.setText("example@email.com")
        binding.edtMobile.setText("13800138000")
        binding.edtBirthday.setText("1990-01-01")
        binding.edtServerExtension.setText("{\"hobby\":\"reading\",\"city\":\"Beijing\"}")
        binding.rbGenderMale.isChecked = true
    }

    /**
     * 更新UI状态
     */
    private fun updateUIStatus() {
        // 更新参数预览
        updateParamsPreview()
        
        // 更新验证状态
        updateValidationStatus()
    }

    /**
     * 更新参数预览
     */
    private fun updateParamsPreview() {
        val updateParams = buildUpdateParams()
        if (updateParams == null) {
            binding.tvParamsPreview.text = "暂无更新参数"
            return
        }

        val preview = StringBuilder()
        preview.append("将要更新的字段:\n")

        if (!updateParams.name.isNullOrEmpty()) {
            preview.append("• 昵称: ${updateParams.name}\n")
        }
        if (!updateParams.avatar.isNullOrEmpty()) {
            preview.append("• 头像: ${updateParams.avatar}\n")
        }
        if (!updateParams.sign.isNullOrEmpty()) {
            preview.append("• 签名: ${updateParams.sign}\n")
        }
        if (!updateParams.email.isNullOrEmpty()) {
            preview.append("• 邮箱: ${updateParams.email}\n")
        }
        if (!updateParams.mobile.isNullOrEmpty()) {
            preview.append("• 手机: ${updateParams.mobile}\n")
        }
        if (!updateParams.birthday.isNullOrEmpty()) {
            preview.append("• 生日: ${updateParams.birthday}\n")
        }
        if (updateParams.gender != null) {
            val genderText = when (updateParams.gender) {
                0 -> "未知"
                1 -> "男性"
                2 -> "女性"
                else -> "自定义(${updateParams.gender})"
            }
            preview.append("• 性别: $genderText\n")
        }
        if (!updateParams.serverExtension.isNullOrEmpty()) {
            preview.append("• 扩展: ${updateParams.serverExtension}")
        }

        binding.tvParamsPreview.text = preview.toString().trimEnd()
    }

    /**
     * 更新验证状态
     */
    private fun updateValidationStatus() {
        val updateParams = buildUpdateParams()
        
        when {
            updateParams == null -> {
                binding.tvValidationStatus.text = "ℹ️ 至少需要设置一个字段才能更新"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.darker_gray))
            }
            hasValidationErrors() -> {
                val errors = getValidationErrors()
                binding.tvValidationStatus.text = "✗ 参数验证失败:\n${errors.joinToString("\n")}"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
            else -> {
                binding.tvValidationStatus.text = "✓ 参数验证通过，可以执行更新"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            }
        }
    }

    /**
     * 检查是否有验证错误
     */
    private fun hasValidationErrors(): Boolean {
        return getValidationErrors().isNotEmpty()
    }

    /**
     * 获取验证错误列表
     */
    private fun getValidationErrors(): List<String> {
        val errors = mutableListOf<String>()
        
        // 检查昵称是否为空串
        val name = binding.edtName.text.toString()
        if (name.isNotEmpty() && name.trim().isEmpty()) {
            errors.add("• 昵称不能为空串")
        }
        
        // 检查头像URL长度
        val avatar = binding.edtAvatar.text.toString()
        if (avatar.length > 1024) {
            errors.add("• 头像URL长度不能超过1024字节")
        }
        
        // 检查签名长度
        val sign = binding.edtSign.text.toString()
        if (sign.length > 256) {
            errors.add("• 个性签名长度不能超过256字符")
        }
        
        // 检查邮箱长度
        val email = binding.edtEmail.text.toString()
        if (email.length > 64) {
            errors.add("• 邮箱长度不能超过64字符")
        }
        
        // 检查手机号长度
        val mobile = binding.edtMobile.text.toString()
        if (mobile.length > 32) {
            errors.add("• 手机号长度不能超过32字符")
        }
        
        // 检查生日长度
        val birthday = binding.edtBirthday.text.toString()
        if (birthday.length > 16) {
            errors.add("• 生日长度不能超过16字符")
        }
        
        // 检查扩展字段长度
        val serverExtension = binding.edtServerExtension.text.toString()
        if (serverExtension.length > 1024) {
            errors.add("• 扩展字段长度不能超过1024字符")
        }
        
        return errors
    }

    /**
     * 构建更新参数
     */
    private fun buildUpdateParams(): V2NIMUserUpdateParams? {
        val builder = V2NIMUserUpdateParams.V2NIMUserUpdateParamsBuilder.builder()
        var hasParams = false

        // 昵称
        val name = binding.edtName.text.toString()
        if (name.isNotEmpty()) {
            builder.withName(name)
            hasParams = true
        }

        // 头像
        val avatar = binding.edtAvatar.text.toString()
        if (avatar.isNotEmpty()) {
            builder.withAvatar(avatar)
            hasParams = true
        }

        // 签名
        val sign = binding.edtSign.text.toString()
        if (sign.isNotEmpty()) {
            builder.withSign(sign)
            hasParams = true
        }

        // 邮箱
        val email = binding.edtEmail.text.toString()
        if (email.isNotEmpty()) {
            builder.withEmail(email)
            hasParams = true
        }

        // 手机
        val mobile = binding.edtMobile.text.toString()
        if (mobile.isNotEmpty()) {
            builder.withMobile(mobile)
            hasParams = true
        }

        // 生日
        val birthday = binding.edtBirthday.text.toString()
        if (birthday.isNotEmpty()) {
            builder.withBirthday(birthday)
            hasParams = true
        }

        // 性别
        val gender = getSelectedGender()
        if (gender != null) {
            builder.withGender(gender)
            hasParams = true
        }

        // 扩展字段
        val serverExtension = binding.edtServerExtension.text.toString()
        if (serverExtension.isNotEmpty()) {
            builder.withServerExtension(serverExtension)
            hasParams = true
        }

        return if (hasParams) builder.build() else null
    }

    /**
     * 获取选中的性别
     */
    private fun getSelectedGender(): Int? {
        return when (binding.rgGender.checkedRadioButtonId) {
            binding.rbGenderUnknown.id -> 0
            binding.rbGenderMale.id -> 1
            binding.rbGenderFemale.id -> 2
            else -> null
        }
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        val updateParams = buildUpdateParams()
        
        if (updateParams == null) {
            showToast("请至少设置一个要更新的字段")
            return
        }

        if (hasValidationErrors()) {
            showToast("参数验证失败，请检查输入")
            return
        }

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMUserService::class.java).updateSelfUserProfile(
            updateParams,
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
            activityViewModel.refresh("更新用户资料失败: $error")
            return
        }

        val resultText = buildResultText()
        activityViewModel.refresh(resultText)
    }

    /**
     * 构建结果文本
     */
    private fun buildResultText(): String {
        val updateParams = buildUpdateParams()
        val sb = StringBuilder()
        
        sb.append("更新用户资料成功:\n\n")
        
        sb.append("✅ 更新操作已完成\n")
        sb.append("📢 SDK将抛出onUserProfileChanged回调\n\n")
        
        sb.append("已更新的字段:\n")

        updateParams?.let { params ->
            if (!params.name.isNullOrEmpty()) {
                sb.append("• 昵称: ${params.name}\n")
            }
            if (!params.avatar.isNullOrEmpty()) {
                sb.append("• 头像: ${params.avatar}\n")
            }
            if (!params.sign.isNullOrEmpty()) {
                sb.append("• 个性签名: ${params.sign}\n")
            }
            if (!params.email.isNullOrEmpty()) {
                sb.append("• 邮箱: ${params.email}\n")
            }
            if (!params.mobile.isNullOrEmpty()) {
                sb.append("• 手机号: ${params.mobile}\n")
            }
            if (!params.birthday.isNullOrEmpty()) {
                sb.append("• 生日: ${params.birthday}\n")
            }
            if (params.gender != null) {
                val genderText = when (params.gender) {
                    0 -> "未知"
                    1 -> "男性"
                    2 -> "女性"
                    else -> "自定义(${params.gender})"
                }
                sb.append("• 性别: $genderText\n")
            }
            if (!params.serverExtension.isNullOrEmpty()) {
                sb.append("• 服务端扩展: ${params.serverExtension}\n")
            }
        }
        
        sb.append("\n📝 注意事项:\n")
        sb.append("• 只有设置的字段被更新，其他字段保持不变\n")
        sb.append("• 更新后本地缓存会自动刷新\n")
        sb.append("• 可以通过用户信息监听器接收变更通知")

        return sb.toString()
    }
}
package com.netease.nim.samples.login

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.netease.nim.samples.base.BaseActivity
import com.netease.nim.samples.databinding.ActivityLoginBinding
import com.netease.nim.samples.main.MainActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginService
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncLevel
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginAuthType
import com.netease.nimlib.sdk.v2.auth.option.V2NIMLoginOption

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.cbLoginOption.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.llLoginOption.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }

        binding.btnLogin.setOnClickListener { v: View? ->
            val userName = binding.edtUserName.text.toString()
            val password = binding.edtPassword.text.toString()
            if (TextUtils.isEmpty(userName)) {
                binding.edtUserName.error = "请输入用户名"
                return@setOnClickListener
            }

            var loginOption: V2NIMLoginOption? = null
            if (binding.cbLoginOption.isChecked) {
                loginOption = V2NIMLoginOption()
                if (binding.cbOptionRetryCount.isChecked) {
                    loginOption.retryCount = binding.edtOptionRetryCount.text.toString().toIntOrNull()
                }
                if (binding.cbOptionTimeout.isChecked) {
                    loginOption.timeout = binding.edtOptionTimeout.text.toString().toLongOrNull()
                }
                loginOption.forceMode = binding.cbOptionForceMode.isChecked
                if (binding.cbOptionAuthType.isChecked) {
                    if(binding.rbAuthTypeDefault.isChecked){
                        loginOption.authType = V2NIMLoginAuthType.V2NIM_LOGIN_AUTH_TYPE_DEFAULT
                    }else if(binding.rbAuthTypeDynamicToken.isChecked){
                        loginOption.authType = V2NIMLoginAuthType.V2NIM_LOGIN_AUTH_TYPE_DYNAMIC_TOKEN
                    }else if(binding.rbAuthTypeThirdParty.isChecked) {
                        loginOption.authType = V2NIMLoginAuthType.V2NIM_LOGIN_AUTH_TYPE_THIRD_PARTY
                    }
                }

                // 如果登录方式为动态token，则需要设置tokenProvider
                if(loginOption.authType == V2NIMLoginAuthType.V2NIM_LOGIN_AUTH_TYPE_DYNAMIC_TOKEN ){
//                    loginOption.tokenProvider = xxx
                }
                // 如果登录方式为第三方，一旦设置了tokenProvider或loginExtensionProvider，不能返回null，
                // 当然也可以不设置tokenProvider或loginExtensionProvider，使用和V2NIM_LOGIN_AUTH_TYPE_DEFAULT的使用方式
                if(loginOption.authType == V2NIMLoginAuthType.V2NIM_LOGIN_AUTH_TYPE_THIRD_PARTY ) {
//                   loginOption.tokenProvider = xxx
//                   loginOption.loginExtensionProvider = xxx
                }
                //登录后的数据同步级别，默认是V2NIM_DATA_SYNC_TYPE_LEVEL_FULL
                //全量同步，耗时较长，部分场景使用情况下已经有数据，体验更好
                //基数数据同步，不影响使用
                loginOption.syncLevel = V2NIMDataSyncLevel.V2NIM_DATA_SYNC_TYPE_LEVEL_FULL
            }

            // 如果登录方式为默认方式，则需要设置密码
            if(loginOption?.authType == V2NIMLoginAuthType.V2NIM_LOGIN_AUTH_TYPE_DEFAULT && TextUtils.isEmpty(password)){
                binding.edtPassword.error = "请输入密码"
                return@setOnClickListener
            }
            showLoadingDialog()
            NIMClient.getService(V2NIMLoginService::class.java).login(userName,password,loginOption,{
                dismissLoadingDialog()
                // 登录成功
                // 跳转到主界面
                MainActivity.startActivity(this@LoginActivity)
                finish()
            },{error ->
                dismissLoadingDialog()
                // 登录失败
                showToast("登录失败，错误码：${error.code},${error.desc}")
            })


        }

        binding.btnEnterMainPage.setOnClickListener {
            MainActivity.startActivity(this@LoginActivity)
        }
    }
}
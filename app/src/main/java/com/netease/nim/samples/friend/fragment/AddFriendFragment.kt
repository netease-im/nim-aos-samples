package com.netease.nim.samples.friend.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentAddFriendBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.friend.V2NIMFriendService
import com.netease.nimlib.sdk.v2.friend.enums.V2NIMFriendAddMode
import com.netease.nimlib.sdk.v2.friend.param.V2NIMFriendAddParams
import java.text.SimpleDateFormat
import java.util.*

class AddFriendFragment : BaseMethodExecuteFragment<FragmentAddFriendBinding>() {

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAddFriendBinding {
        return FragmentAddFriendBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        val accountIds = getAccountId()
        if (accountIds.isEmpty()) {
            showToast("请输入用户账号ID")
            return
        }
        val pos = binding.edtPostscript.text;
        val joinMode = if(binding.rbAdd.isChecked)
        V2NIMFriendAddMode.V2NIM_FRIEND_MODE_TYPE_ADD;
        else
        V2NIMFriendAddMode.V2NIM_FRIEND_MODE_TYPE_APPLY;
        val params = V2NIMFriendAddParams.V2NIMFriendAddParamsBuilder.builder(joinMode).withPostscript(pos
            .toString())
        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMFriendService::class.java).addFriend(
            accountIds,params.build(),
            { userList -> updateResult(joinMode,null) },
            { error -> updateResult(joinMode,error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(model:V2NIMFriendAddMode,error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (error != null) {
            activityViewModel.refresh("添加好友失败: $error")
            return
        }
        if (model == V2NIMFriendAddMode.V2NIM_FRIEND_MODE_TYPE_ADD) {
            showToast("添加好友成功")
        }else{
            showToast("好友添加申请已发送")
        }
    }



    /**
     * 获取用户账号
     */
    private fun getAccountId(): String {
        val idsText = binding.edtAccountIds.text.toString().trim()
        return idsText.trim()
    }
}
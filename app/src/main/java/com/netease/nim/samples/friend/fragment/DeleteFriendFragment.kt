package com.netease.nim.samples.friend.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentAddFriendBinding
import com.netease.nim.samples.databinding.FragmentDeleteFriendBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.friend.V2NIMFriendService
import com.netease.nimlib.sdk.v2.friend.enums.V2NIMFriendAddMode
import com.netease.nimlib.sdk.v2.friend.param.V2NIMFriendAddParams
import com.netease.nimlib.sdk.v2.friend.param.V2NIMFriendDeleteParams
import java.text.SimpleDateFormat
import java.util.*

class DeleteFriendFragment : BaseMethodExecuteFragment<FragmentDeleteFriendBinding>() {

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDeleteFriendBinding {
        return FragmentDeleteFriendBinding.inflate(inflater, container, false)
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
        val deleteAlias = binding.rbDeleteAlias.isChecked
        V2NIMFriendAddMode.V2NIM_FRIEND_MODE_TYPE_APPLY;
        val params = V2NIMFriendDeleteParams.V2NIMFriendDeleteParamsBuilder.builder().withDeleteAlias(deleteAlias)
        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMFriendService::class.java).deleteFriend(
            accountIds,params.build(),
            { userList -> updateResult(null) },
            { error -> updateResult(error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (error != null) {
            activityViewModel.refresh("好友删除失败: $error")
            return
        }
        showToast("好友删除成功")
    }



    /**
     * 获取用户账号
     */
    private fun getAccountId(): String {
        val idsText = binding.edtAccountIds.text.toString().trim()
        return idsText.trim()
    }
}
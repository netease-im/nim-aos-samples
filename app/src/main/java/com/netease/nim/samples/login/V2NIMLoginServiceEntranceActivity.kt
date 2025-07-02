package com.netease.nim.samples.login

import com.netease.nim.samples.base.BaseMethodExecuteActivity
import com.netease.nim.samples.base.list.BaseListActivity
import com.netease.nim.samples.base.list.model.ItemModel
import com.netease.nim.samples.login.constants.V2NIMLoginServiceConstants
import com.netease.nim.samples.main.model.MethodItemModel

class V2NIMLoginServiceEntranceActivity : BaseListActivity() {

    override fun setContentList(): MutableList<ItemModel> {
        val result: MutableList<ItemModel> = ArrayList()
        result.add(
            MethodItemModel(
                V2NIMLoginServiceConstants.METHOD_logout,
                V2NIMLoginServiceConstants.DESC_logout
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLoginServiceConstants.METHOD_getLoginUser,
                V2NIMLoginServiceConstants.DESC_getLoginUser
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLoginServiceConstants.METHOD_getLoginStatus,
                V2NIMLoginServiceConstants.DESC_getLoginStatus
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLoginServiceConstants.METHOD_getCurrentLoginClient,
                V2NIMLoginServiceConstants.DESC_getCurrentLoginClient
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLoginServiceConstants.METHOD_getLoginClients,
                V2NIMLoginServiceConstants.DESC_getLoginClients
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLoginServiceConstants.METHOD_kickOffline,
                V2NIMLoginServiceConstants.DESC_kickOffline
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLoginServiceConstants.METHOD_getKickedOfflineDetail,
                V2NIMLoginServiceConstants.DESC_getKickedOfflineDetail
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLoginServiceConstants.METHOD_getConnectStatus,
                V2NIMLoginServiceConstants.DESC_getConnectStatus
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLoginServiceConstants.METHOD_getDataSync,
                V2NIMLoginServiceConstants.DESC_getDataSync
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLoginServiceConstants.METHOD_getChatroomLinkAddress,
                V2NIMLoginServiceConstants.DESC_getChatroomLinkAddress
            )
        )
//        result.add(
//            MethodItemModel(
//                V2NIMLoginServiceConstants.METHOD_setReconnectDelayProvider,
//                V2NIMLoginServiceConstants.DESC_setReconnectDelayProvider
//            )
//        )
        result.add(
            MethodItemModel(
                V2NIMLoginServiceConstants.METHOD_addLoginListener,
                V2NIMLoginServiceConstants.DESC_addLoginListener
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLoginServiceConstants.METHOD_removeLoginListener,
                V2NIMLoginServiceConstants.DESC_removeLoginListener
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLoginServiceConstants.METHOD_addLoginDetailListener,
                V2NIMLoginServiceConstants.DESC_addLoginDetailListener
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLoginServiceConstants.METHOD_removeLoginDetailListener,
                V2NIMLoginServiceConstants.DESC_removeLoginDetailListener
            )
        )

        return result
    }

    override fun onRecycleViewItemClick(position: Int, model: ItemModel) {
        super.onRecycleViewItemClick(position, model)
        val methodItemModel = model as MethodItemModel
        // 跳转到具体的方法执行Activity
        BaseMethodExecuteActivity.startActivity<V2NIMLoginServiceActivity>(
            this,
            methodItemModel.methodName
        )
    }

    companion object {
        private const val TAG = "V2NIMLoginServiceEntranceActivity"
    }
}
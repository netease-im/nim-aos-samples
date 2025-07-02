package com.netease.nim.samples.main.fragment

import android.content.Intent
import com.netease.nim.samples.base.list.BaseListFragment
import com.netease.nim.samples.base.list.model.ItemModel
import com.netease.nim.samples.login.V2NIMLoginServiceEntranceActivity
import com.netease.nim.samples.main.model.MethodItemModel


class V2NIMMoreFragment : BaseListFragment() {
    override fun setContentList(): List<ItemModel>? {
        val result: MutableList<ItemModel> = ArrayList()
        result.add(MethodItemModel(SERVICE_V2NIMLoginService, DESC_V2NIMLoginService))
        result.add(MethodItemModel(SERVICE_V2NIMTeamService, DESC_V2NIMTeamService))
        result.add(MethodItemModel(SERVICE_V2NIMAIService, DESC_V2NIMAIService))
        result.add(MethodItemModel(SERVICE_V2NIMConversationService, DESC_V2NIMConversationService))
        result.add(
            MethodItemModel(
                SERVICE_V2NIMConversationGroupService,
                DESC_V2NIMConversationGroupService
            )
        )
        result.add(MethodItemModel(SERVICE_V2NIMNotificationService, DESC_V2NIMNotificationService))
        result.add(MethodItemModel(SERVICE_V2NIMPassthroughService, DESC_V2NIMPassthroughService))
        result.add(MethodItemModel(SERVICE_V2NIMSettingService, DESC_V2NIMSettingService))
        result.add(MethodItemModel(SERVICE_V2NIMSignallingService, DESC_V2NIMSignallingService))
        result.add(MethodItemModel(SERVICE_V2NIMStorageService, DESC_V2NIMStorageService))
        result.add(MethodItemModel(SERVICE_V2NIMSubscriptionService, DESC_V2NIMSubscriptionService))
        result.add(MethodItemModel(SERVICE_V2NIMChatroomService, DESC_V2NIMChatroomService))
        return result
    }

    override fun onRecycleViewItemClick(position: Int, model: ItemModel) {
        super.onRecycleViewItemClick(position, model)
        val methodItemModel = model as MethodItemModel

        when (methodItemModel.methodName) {
            SERVICE_V2NIMLoginService -> startActivity(
                Intent(
                    context,
                    V2NIMLoginServiceEntranceActivity::class.java
                )
            )

            SERVICE_V2NIMTeamService -> {
                showToast("待实现")
            }
            SERVICE_V2NIMAIService -> {
                showToast("待实现")
            }
            SERVICE_V2NIMConversationService -> {
                showToast("待实现")
            }
            SERVICE_V2NIMConversationGroupService -> {
                showToast("待实现")
            }
            SERVICE_V2NIMNotificationService -> {
                showToast("待实现")
            }
            SERVICE_V2NIMPassthroughService -> {
                showToast("待实现")
            }
            SERVICE_V2NIMSettingService -> {
                showToast("待实现")
            }
            SERVICE_V2NIMSignallingService -> {
                showToast("待实现")
            }
            SERVICE_V2NIMStorageService -> {
                showToast("待实现")
            }
            SERVICE_V2NIMSubscriptionService -> {
                showToast("待实现")
            }
            SERVICE_V2NIMChatroomService -> {
                showToast("待实现")
            }
            else -> {}
        }
    }

    companion object {
        private const val TAG = "V2NIMMoreFragment"


        private const val SERVICE_V2NIMLoginService = "V2NIMLoginService"
        private const val SERVICE_V2NIMTeamService = "V2NIMTeamService"
        private const val SERVICE_V2NIMAIService = "V2NIMAIService"
        private const val SERVICE_V2NIMConversationService = "V2NIMConversationService"
        private const val SERVICE_V2NIMConversationGroupService = "V2NIMConversationGroupService"
        private const val SERVICE_V2NIMNotificationService = "V2NIMNotificationService"
        private const val SERVICE_V2NIMPassthroughService = "V2NIMPassthroughService"
        private const val SERVICE_V2NIMSettingService = "V2NIMSettingService"
        private const val SERVICE_V2NIMSignallingService = "V2NIMSignallingService"
        private const val SERVICE_V2NIMStorageService = "V2NIMStorageService"
        private const val SERVICE_V2NIMSubscriptionService = "V2NIMSubscriptionService"
        private const val SERVICE_V2NIMChatroomService = "V2NIMChatroomService"

        private const val DESC_V2NIMLoginService = "登录服务"
        private const val DESC_V2NIMTeamService = "群组服务"
        private const val DESC_V2NIMAIService = "AI数字人服务"
        private const val DESC_V2NIMConversationService = "云端会话服务"
        private const val DESC_V2NIMConversationGroupService = "云端会话分组服务"
        private const val DESC_V2NIMNotificationService = "通知服务"
        private const val DESC_V2NIMPassthroughService = "透传服务"
        private const val DESC_V2NIMSettingService = "设置服务"
        private const val DESC_V2NIMSignallingService = "信令服务"
        private const val DESC_V2NIMStorageService = "存储服务"
        private const val DESC_V2NIMSubscriptionService = "订阅服务"
        private const val DESC_V2NIMChatroomService = "聊天室服务"
    }
}

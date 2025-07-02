package com.netease.nim.samples.main.fragment

import com.netease.nim.samples.base.BaseMethodExecuteActivity
import com.netease.nim.samples.base.list.BaseListFragment
import com.netease.nim.samples.base.list.model.ItemModel
import com.netease.nim.samples.localconversation.V2NIMLocalConversationServiceActivity
import com.netease.nim.samples.localconversation.constants.V2NIMLocalConversationServiceConstants
import com.netease.nim.samples.main.model.MethodItemModel


class V2NIMLocalConversationServiceFragment : BaseListFragment() {
    override fun setContentList(): List<ItemModel>? {
        val result: MutableList<ItemModel> = ArrayList()
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_getConversationList,
                V2NIMLocalConversationServiceConstants.DESC_getConversationList
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_getConversationListByOption,
                V2NIMLocalConversationServiceConstants.DESC_getConversationListByOption
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_getConversation,
                V2NIMLocalConversationServiceConstants.DESC_getConversation
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_getConversationListByIds,
                V2NIMLocalConversationServiceConstants.DESC_getConversationListByIds
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_getStickTopConversationList,
                V2NIMLocalConversationServiceConstants.DESC_getStickTopConversationList
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_createConversation,
                V2NIMLocalConversationServiceConstants.DESC_createConversation
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_deleteConversation,
                V2NIMLocalConversationServiceConstants.DESC_deleteConversation
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_deleteConversationListByIds,
                V2NIMLocalConversationServiceConstants.DESC_deleteConversationListByIds
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_stickTopConversation,
                V2NIMLocalConversationServiceConstants.DESC_stickTopConversation
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_updateConversationLocalExtension,
                V2NIMLocalConversationServiceConstants.DESC_updateConversationLocalExtension
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_getTotalUnreadCount,
                V2NIMLocalConversationServiceConstants.DESC_getTotalUnreadCount
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_getUnreadCountByIds,
                V2NIMLocalConversationServiceConstants.DESC_getUnreadCountByIds
            )
        )
//        result.add(
//            MethodItemModel(
//                V2NIMLocalConversationServiceConstants.METHOD_getUnreadCountByFilter,
//                V2NIMLocalConversationServiceConstants.DESC_getUnreadCountByFilter
//            )
//        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_clearTotalUnreadCount,
                V2NIMLocalConversationServiceConstants.DESC_clearTotalUnreadCount
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_clearUnreadCountByIds,
                V2NIMLocalConversationServiceConstants.DESC_clearUnreadCountByIds
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_clearUnreadCountByTypes,
                V2NIMLocalConversationServiceConstants.DESC_clearUnreadCountByTypes
            )
        )
//        result.add(
//            MethodItemModel(
//                V2NIMLocalConversationServiceConstants.METHOD_subscribeUnreadCountByFilter,
//                V2NIMLocalConversationServiceConstants.DESC_subscribeUnreadCountByFilter
//            )
//        )
//        result.add(
//            MethodItemModel(
//                V2NIMLocalConversationServiceConstants.METHOD_unsubscribeUnreadCountByFilter,
//                V2NIMLocalConversationServiceConstants.DESC_unsubscribeUnreadCountByFilter
//            )
//        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_getConversationReadTime,
                V2NIMLocalConversationServiceConstants.DESC_getConversationReadTime
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_markConversationRead,
                V2NIMLocalConversationServiceConstants.DESC_markConversationRead
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_addConversationListener,
                V2NIMLocalConversationServiceConstants.DESC_addConversationListener
            )
        )
        result.add(
            MethodItemModel(
                V2NIMLocalConversationServiceConstants.METHOD_removeConversationListener,
                V2NIMLocalConversationServiceConstants.DESC_removeConversationListener
            )
        )
        return result
    }

    override fun onRecycleViewItemClick(position: Int, model: ItemModel) {
        super.onRecycleViewItemClick(position, model)
        val methodItemModel = model as MethodItemModel
        activity?.apply {
            BaseMethodExecuteActivity.startActivity<V2NIMLocalConversationServiceActivity>(this, methodItemModel.methodName)
        }

    }

    companion object {
        private const val TAG = "V2NIMLocalConversationServiceFragment"
    }
}

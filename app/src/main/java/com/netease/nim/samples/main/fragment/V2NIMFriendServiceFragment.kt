package com.netease.nim.samples.main.fragment

import com.netease.nim.samples.base.BaseMethodExecuteActivity
import com.netease.nim.samples.base.list.BaseListFragment
import com.netease.nim.samples.base.list.model.ItemModel
import com.netease.nim.samples.friend.V2NIMFriendServiceActivity
import com.netease.nim.samples.friend.constants.V2NIMFriendServiceConstants
import com.netease.nim.samples.main.model.MethodItemModel
import com.netease.nim.samples.message.V2NIMMessageServiceActivity


class V2NIMFriendServiceFragment : BaseListFragment() {
    override fun setContentList(): List<ItemModel>? {
        val result: MutableList<ItemModel> = ArrayList()
        result.add(
            MethodItemModel(
                V2NIMFriendServiceConstants.METHOD_addFriend,
                V2NIMFriendServiceConstants.DESC_addFriend
            )
        )
        result.add(
            MethodItemModel(
                V2NIMFriendServiceConstants.METHOD_deleteFriend,
                V2NIMFriendServiceConstants.DESC_deleteFriend
            )
        )
        result.add(
            MethodItemModel(
                V2NIMFriendServiceConstants.METHOD_acceptAddApplication,
                V2NIMFriendServiceConstants.DESC_acceptAddApplication
            )
        )
        result.add(
            MethodItemModel(
                V2NIMFriendServiceConstants.METHOD_rejectAddApplication,
                V2NIMFriendServiceConstants.DESC_rejectAddApplication
            )
        )
        result.add(
            MethodItemModel(
                V2NIMFriendServiceConstants.METHOD_setFriendInfo,
                V2NIMFriendServiceConstants.DESC_setFriendInfo
            )
        )
        result.add(
            MethodItemModel(
                V2NIMFriendServiceConstants.METHOD_getFriendList,
                V2NIMFriendServiceConstants.DESC_getFriendList
            )
        )
        result.add(
            MethodItemModel(
                V2NIMFriendServiceConstants.METHOD_getFriendByIds,
                V2NIMFriendServiceConstants.DESC_getFriendByIds
            )
        )
        result.add(
            MethodItemModel(
                V2NIMFriendServiceConstants.METHOD_checkFriend,
                V2NIMFriendServiceConstants.DESC_checkFriend
            )
        )
        result.add(
            MethodItemModel(
                V2NIMFriendServiceConstants.METHOD_getAddApplicationList,
                V2NIMFriendServiceConstants.DESC_getAddApplicationList
            )
        )
        result.add(
            MethodItemModel(
                V2NIMFriendServiceConstants.METHOD_getAddApplicationUnreadCount,
                V2NIMFriendServiceConstants.DESC_getAddApplicationUnreadCount
            )
        )
        result.add(
            MethodItemModel(
                V2NIMFriendServiceConstants.METHOD_setAddApplicationRead,
                V2NIMFriendServiceConstants.DESC_setAddApplicationRead
            )
        )
        result.add(
            MethodItemModel(
                V2NIMFriendServiceConstants.METHOD_searchFriendByOption,
                V2NIMFriendServiceConstants.DESC_searchFriendByOption
            )
        )
        result.add(
            MethodItemModel(
                V2NIMFriendServiceConstants.METHOD_clearAllAddApplication,
                V2NIMFriendServiceConstants.DESC_clearAllAddApplication
            )
        )
        result.add(
            MethodItemModel(
                V2NIMFriendServiceConstants.METHOD_deleteAddApplication,
                V2NIMFriendServiceConstants.DESC_deleteAddApplication
            )
        )
        result.add(
            MethodItemModel(
                V2NIMFriendServiceConstants.METHOD_addFriendListener,
                V2NIMFriendServiceConstants.DESC_addFriendListener
            )
        )
        result.add(
            MethodItemModel(
                V2NIMFriendServiceConstants.METHOD_removeFriendListener,
                V2NIMFriendServiceConstants.DESC_removeFriendListener
            )
        )
        return result
    }


    override fun onRecycleViewItemClick(position: Int, model: ItemModel) {
        super.onRecycleViewItemClick(position, model)
        val methodItemModel = model as MethodItemModel
        activity?.apply {
            BaseMethodExecuteActivity.startActivity<V2NIMFriendServiceActivity>(this, methodItemModel.methodName)
        }
    }


    companion object {
        private const val TAG = "V2NIMFriendServiceFragment"
    }
}
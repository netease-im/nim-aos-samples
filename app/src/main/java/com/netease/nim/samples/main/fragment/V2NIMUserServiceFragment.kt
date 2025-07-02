package com.netease.nim.samples.main.fragment

import com.netease.nim.samples.base.BaseMethodExecuteActivity
import com.netease.nim.samples.base.list.BaseListFragment
import com.netease.nim.samples.base.list.model.ItemModel
import com.netease.nim.samples.friend.V2NIMFriendServiceActivity
import com.netease.nim.samples.main.model.MethodItemModel
import com.netease.nim.samples.user.V2NIMUserServiceActivity
import com.netease.nim.samples.user.constants.V2NIMUserServiceConstants


class V2NIMUserServiceFragment : BaseListFragment() {
    override fun setContentList(): List<ItemModel>? {
        val result: MutableList<ItemModel> = ArrayList()
        result.add(
            MethodItemModel(
                V2NIMUserServiceConstants.METHOD_getUserList,
                V2NIMUserServiceConstants.DESC_getUserList
            )
        )
        result.add(
            MethodItemModel(
                V2NIMUserServiceConstants.METHOD_getUserListFromCloud,
                V2NIMUserServiceConstants.DESC_getUserListFromCloud
            )
        )
        result.add(
            MethodItemModel(
                V2NIMUserServiceConstants.METHOD_updateSelfUserProfile,
                V2NIMUserServiceConstants.DESC_updateSelfUserProfile
            )
        )
        result.add(
            MethodItemModel(
                V2NIMUserServiceConstants.METHOD_addUserToBlockList,
                V2NIMUserServiceConstants.DESC_addUserToBlockList
            )
        )
        result.add(
            MethodItemModel(
                V2NIMUserServiceConstants.METHOD_removeUserFromBlockList,
                V2NIMUserServiceConstants.DESC_removeUserFromBlockList
            )
        )
        result.add(
            MethodItemModel(
                V2NIMUserServiceConstants.METHOD_getBlockList,
                V2NIMUserServiceConstants.DESC_getBlockList
            )
        )
        result.add(
            MethodItemModel(
                V2NIMUserServiceConstants.METHOD_checkBlock,
                V2NIMUserServiceConstants.DESC_checkBlock
            )
        )
        result.add(
            MethodItemModel(
                V2NIMUserServiceConstants.METHOD_searchUserByOption,
                V2NIMUserServiceConstants.DESC_searchUserByOption
            )
        )
        result.add(
            MethodItemModel(
                V2NIMUserServiceConstants.METHOD_addUserListener,
                V2NIMUserServiceConstants.DESC_addUserListener
            )
        )
        result.add(
            MethodItemModel(
                V2NIMUserServiceConstants.METHOD_removeUserListener,
                V2NIMUserServiceConstants.DESC_removeUserListener
            )
        )
        return result
    }

    override fun onRecycleViewItemClick(position: Int, model: ItemModel) {
        super.onRecycleViewItemClick(position, model)
        val methodItemModel = model as MethodItemModel
        activity?.apply {
            BaseMethodExecuteActivity.startActivity<V2NIMUserServiceActivity>(this, methodItemModel.methodName)
        }
    }

    companion object {
        private const val TAG = "V2NIMUserServiceFragment"
    }
}
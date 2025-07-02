package com.netease.nim.samples.friend

import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.base.BaseMethodExecuteActivity
import com.netease.nim.samples.friend.constants.V2NIMFriendServiceConstants
import com.netease.nim.samples.friend.fragment.AcceptFriendApplicationFragment
import com.netease.nim.samples.friend.fragment.AddFriendFragment
import com.netease.nim.samples.friend.fragment.AddFriendListenerFragment
import com.netease.nim.samples.friend.fragment.ClearAllApplicationFragment
import com.netease.nim.samples.friend.fragment.DeleteFriendApplicationFragment
import com.netease.nim.samples.friend.fragment.DeleteFriendFragment
import com.netease.nim.samples.friend.fragment.GetAddApplicationListFragment
import com.netease.nim.samples.friend.fragment.GetApplicationUnreadCountFragment
import com.netease.nim.samples.friend.fragment.GetFriendListByIdsFragment
import com.netease.nim.samples.friend.fragment.GetFriendListFragment
import com.netease.nim.samples.friend.fragment.RejectFriendApplicationFragment
import com.netease.nim.samples.friend.fragment.RemoveFriendListenerFragment
import com.netease.nim.samples.friend.fragment.SetApplicationReadFragment
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment

class V2NIMFriendServiceActivity : BaseMethodExecuteActivity() {
    override fun initFragment(): BaseMethodExecuteFragment<*>? {
        when (methodName) {
            V2NIMFriendServiceConstants.METHOD_addFriend -> {
                return AddFriendFragment()
            }
            V2NIMFriendServiceConstants.METHOD_deleteFriend -> {
                return DeleteFriendFragment()
            }
            V2NIMFriendServiceConstants.METHOD_acceptAddApplication -> {
                return AcceptFriendApplicationFragment()
            }
            V2NIMFriendServiceConstants.METHOD_rejectAddApplication -> {
                return RejectFriendApplicationFragment()
            }
//            V2NIMFriendServiceConstants.METHOD_setFriendInfo -> {}
            V2NIMFriendServiceConstants.METHOD_getFriendList -> {
                return GetFriendListFragment()
            }
            V2NIMFriendServiceConstants.METHOD_getFriendByIds -> {
                return GetFriendListByIdsFragment()
            }
//            V2NIMFriendServiceConstants.METHOD_checkFriend -> {}
            V2NIMFriendServiceConstants.METHOD_getAddApplicationList -> {
                return GetAddApplicationListFragment()
            }
            V2NIMFriendServiceConstants.METHOD_getAddApplicationUnreadCount -> {

                return GetApplicationUnreadCountFragment()
            }
            V2NIMFriendServiceConstants.METHOD_setAddApplicationRead -> {
                return SetApplicationReadFragment()
            }
            V2NIMFriendServiceConstants.METHOD_searchFriendByOption -> {}
            V2NIMFriendServiceConstants.METHOD_clearAllAddApplication -> {
                return ClearAllApplicationFragment()
            }
            V2NIMFriendServiceConstants.METHOD_deleteAddApplication -> {
                return DeleteFriendApplicationFragment()
            }
            V2NIMFriendServiceConstants.METHOD_addFriendListener -> {
                return AddFriendListenerFragment()
            }
            V2NIMFriendServiceConstants.METHOD_removeFriendListener -> {
                return RemoveFriendListenerFragment()
            }
            else -> return null
        }
        return null
    }

    override fun addFragment(fragment: String, params: Array<Any?>?) {
        // 这里可以添加其他Fragment的逻辑，类似于V2NIMMessageServiceActivity中的addFragment方法
        // 例如好友选择、好友多选等Fragment
        when (fragment) {
            FriendApplicationSelectFragment.NAME -> addFragment(
                FriendApplicationSelectFragment(),fragment
            )
            else -> {

            }
        }
    }
}
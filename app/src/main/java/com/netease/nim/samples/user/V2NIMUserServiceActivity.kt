package com.netease.nim.samples.user

import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.base.BaseMethodExecuteActivity
import com.netease.nim.samples.user.constants.V2NIMUserServiceConstants
import com.netease.nim.samples.user.fragment.AddUserListenerFragment
import com.netease.nim.samples.user.fragment.AddUserToBlockListFragment
import com.netease.nim.samples.user.fragment.CheckBlockFragment
import com.netease.nim.samples.user.fragment.GetBlockListFragment
import com.netease.nim.samples.user.fragment.GetUserListFragment
import com.netease.nim.samples.user.fragment.GetUserListFromCloudFragment
import com.netease.nim.samples.user.fragment.RemoveUserFromBlockListFragment
import com.netease.nim.samples.user.fragment.RemoveUserListenerFragment
import com.netease.nim.samples.user.fragment.SearchUserByOptionFragment
import com.netease.nim.samples.user.fragment.UpdateSelfUserProfileFragment

class V2NIMUserServiceActivity : BaseMethodExecuteActivity() {
    override fun initFragment(): BaseMethodExecuteFragment<*>? {
        when (methodName) {
            V2NIMUserServiceConstants.METHOD_getUserList -> {
                return GetUserListFragment()
            }

            V2NIMUserServiceConstants.METHOD_getUserListFromCloud -> {
                return GetUserListFromCloudFragment()
            }
            V2NIMUserServiceConstants.METHOD_updateSelfUserProfile -> {
                return UpdateSelfUserProfileFragment()
            }
            V2NIMUserServiceConstants.METHOD_addUserToBlockList -> {
                return AddUserToBlockListFragment()
            }
            V2NIMUserServiceConstants.METHOD_removeUserFromBlockList -> {
                return RemoveUserFromBlockListFragment()
            }
            V2NIMUserServiceConstants.METHOD_getBlockList -> {
                return GetBlockListFragment()
            }
            V2NIMUserServiceConstants.METHOD_checkBlock -> {
                return CheckBlockFragment()
            }
            V2NIMUserServiceConstants.METHOD_searchUserByOption -> {
                return SearchUserByOptionFragment()
            }
            V2NIMUserServiceConstants.METHOD_addUserListener -> {
                return AddUserListenerFragment()
            }
            V2NIMUserServiceConstants.METHOD_removeUserListener -> {
                return RemoveUserListenerFragment()
            }
            else -> return null
        }
        return null
    }

    override fun addFragment(fragment: String, params: Array<Any?>?) {
        // 这里可以添加其他Fragment的逻辑，类似于V2NIMMessageServiceActivity中的addFragment方法
        // 例如用户选择、用户多选等Fragment
        when (fragment) {
            else -> {}
        }
    }
}
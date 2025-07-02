package com.netease.nim.samples.user.constants

object V2NIMUserServiceConstants {
    // 方法名常量
    const val METHOD_getUserList: String = "getUserList"
    const val METHOD_getUserListFromCloud: String = "getUserListFromCloud"
    const val METHOD_updateSelfUserProfile: String = "updateSelfUserProfile"
    const val METHOD_addUserToBlockList: String = "addUserToBlockList"
    const val METHOD_removeUserFromBlockList: String = "removeUserFromBlockList"
    const val METHOD_getBlockList: String = "getBlockList"
    const val METHOD_checkBlock: String = "checkBlock"
    const val METHOD_searchUserByOption: String = "searchUserByOption"
    const val METHOD_addUserListener: String = "addUserListener"
    const val METHOD_removeUserListener: String = "removeUserListener"

    // 方法描述常量
    const val DESC_getUserList: String = "根据用户账号列表获取用户资料"
    const val DESC_getUserListFromCloud: String = "根据用户账号列表从服务器获取用户资料"
    const val DESC_updateSelfUserProfile: String = "更新自己的用户资料"
    const val DESC_addUserToBlockList: String = "添加用户到黑名单中"
    const val DESC_removeUserFromBlockList: String = "从黑名单中移除用户"
    const val DESC_getBlockList: String = "获取黑名单列表"
    const val DESC_checkBlock: String = "查看是否在黑名单"
    const val DESC_searchUserByOption: String = "根据关键字搜索用户信息"
    const val DESC_addUserListener: String = "添加用户资料监听器"
    const val DESC_removeUserListener: String = "移除用户资料监听器"
}
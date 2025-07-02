package com.netease.nim.samples.login

import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.base.BaseMethodExecuteActivity
import com.netease.nim.samples.login.constants.V2NIMLoginServiceConstants
import com.netease.nim.samples.login.fragment.AddLoginDetailListenerFragment
import com.netease.nim.samples.login.fragment.GetChatroomLinkAddressFragment
import com.netease.nim.samples.login.fragment.GetConnectStatusFragment
import com.netease.nim.samples.login.fragment.GetCurrentLoginClientFragment
import com.netease.nim.samples.login.fragment.GetDataSyncFragment
import com.netease.nim.samples.login.fragment.GetKickedOfflineDetailFragment
import com.netease.nim.samples.login.fragment.GetLoginClientsFragment
import com.netease.nim.samples.login.fragment.GetLoginStatusFragment
import com.netease.nim.samples.login.fragment.GetLoginUserFragment
import com.netease.nim.samples.login.fragment.KickOfflineFragment
import com.netease.nim.samples.login.fragment.LogoutFragment
import com.netease.nim.samples.login.fragment.AddLoginListenerFragment
import com.netease.nim.samples.login.fragment.RemoveLoginDetailListenerFragment
import com.netease.nim.samples.login.fragment.RemoveLoginListenerFragment

class V2NIMLoginServiceActivity : BaseMethodExecuteActivity() {

    override fun initFragment(): BaseMethodExecuteFragment<*>? {
        when (methodName) {
            V2NIMLoginServiceConstants.METHOD_logout -> {
                return LogoutFragment()
            }

            V2NIMLoginServiceConstants.METHOD_getLoginUser -> {
                return GetLoginUserFragment()
            }

            V2NIMLoginServiceConstants.METHOD_getLoginStatus -> {
                return GetLoginStatusFragment()
            }

            V2NIMLoginServiceConstants.METHOD_getCurrentLoginClient -> {
                return GetCurrentLoginClientFragment()
            }

            V2NIMLoginServiceConstants.METHOD_getLoginClients -> {
                return GetLoginClientsFragment()
            }

            V2NIMLoginServiceConstants.METHOD_kickOffline -> {
                return KickOfflineFragment()
            }

            V2NIMLoginServiceConstants.METHOD_getKickedOfflineDetail -> {
                return GetKickedOfflineDetailFragment()
            }

            V2NIMLoginServiceConstants.METHOD_getConnectStatus -> {
                return GetConnectStatusFragment()
            }

            V2NIMLoginServiceConstants.METHOD_getDataSync -> {
                return GetDataSyncFragment()
            }

            V2NIMLoginServiceConstants.METHOD_getChatroomLinkAddress -> {
                return GetChatroomLinkAddressFragment()
            }

//            V2NIMLoginServiceConstants.METHOD_setReconnectDelayProvider -> {
//
//                return null
//            }

            V2NIMLoginServiceConstants.METHOD_addLoginListener -> {
                return AddLoginListenerFragment()
            }

            V2NIMLoginServiceConstants.METHOD_removeLoginListener -> {

                return RemoveLoginListenerFragment()
            }

            V2NIMLoginServiceConstants.METHOD_addLoginDetailListener -> {

                return AddLoginDetailListenerFragment()
            }

            V2NIMLoginServiceConstants.METHOD_removeLoginDetailListener -> {

                return RemoveLoginDetailListenerFragment()
            }

            else -> {
                return null
            }
        }
    }

    override fun addFragment(fragment: String, params: Array<Any?>?) {
        // 目前登录服务不需要额外的Fragment选择器
        // 如果需要，可以在这里添加类似消息服务的Fragment处理逻辑
    }
}
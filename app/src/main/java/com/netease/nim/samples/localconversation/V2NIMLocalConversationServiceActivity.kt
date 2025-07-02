package com.netease.nim.samples.localconversation

import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.base.BaseMethodExecuteActivity
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationMultiSelectFragment
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment
import com.netease.nim.samples.localconversation.constants.V2NIMLocalConversationServiceConstants
import com.netease.nim.samples.localconversation.fragment.AddConversationListenerFragment
import com.netease.nim.samples.localconversation.fragment.ClearTotalUnreadCountFragment
import com.netease.nim.samples.localconversation.fragment.ClearUnreadCountByIdsFragment
import com.netease.nim.samples.localconversation.fragment.ClearUnreadCountByTypesFragment
import com.netease.nim.samples.localconversation.fragment.CreateConversationFragment
import com.netease.nim.samples.localconversation.fragment.DeleteConversationFragment
import com.netease.nim.samples.localconversation.fragment.DeleteConversationListByIdsFragment
import com.netease.nim.samples.localconversation.fragment.GetConversationFragment
import com.netease.nim.samples.localconversation.fragment.GetConversationListByIdsFragment
import com.netease.nim.samples.localconversation.fragment.GetConversationListByOptionFragment
import com.netease.nim.samples.localconversation.fragment.GetConversationListFragment
import com.netease.nim.samples.localconversation.fragment.GetConversationReadTimeFragment
import com.netease.nim.samples.localconversation.fragment.GetStickTopConversationListFragment
import com.netease.nim.samples.localconversation.fragment.StickTopConversationFragment
import com.netease.nim.samples.localconversation.fragment.UpdateConversationLocalExtensionFragment
import com.netease.nim.samples.localconversation.fragment.GetTotalUnreadCountFragment
import com.netease.nim.samples.localconversation.fragment.GetUnreadCountByIdsFragment
import com.netease.nim.samples.localconversation.fragment.MarkConversationReadFragment
import com.netease.nim.samples.localconversation.fragment.RemoveConversationListenerFragment

class V2NIMLocalConversationServiceActivity : BaseMethodExecuteActivity() {


    override fun initFragment(): BaseMethodExecuteFragment<*>? {
        when (methodName) {
            V2NIMLocalConversationServiceConstants.METHOD_getConversationList -> {
                return GetConversationListFragment()
            }
            V2NIMLocalConversationServiceConstants.METHOD_getConversationListByOption -> {
                return GetConversationListByOptionFragment()
            }
            V2NIMLocalConversationServiceConstants.METHOD_getConversation -> {
                return GetConversationFragment()
            }
            V2NIMLocalConversationServiceConstants.METHOD_getConversationListByIds -> {
                return GetConversationListByIdsFragment()
            }
            V2NIMLocalConversationServiceConstants.METHOD_getStickTopConversationList -> {
                return GetStickTopConversationListFragment()
            }
            V2NIMLocalConversationServiceConstants.METHOD_createConversation -> {
                return CreateConversationFragment()
            }
            V2NIMLocalConversationServiceConstants.METHOD_deleteConversation -> {
                return DeleteConversationFragment()
            }
            V2NIMLocalConversationServiceConstants.METHOD_deleteConversationListByIds -> {
                return DeleteConversationListByIdsFragment()
            }
            V2NIMLocalConversationServiceConstants.METHOD_stickTopConversation -> {
                return StickTopConversationFragment()
            }
            V2NIMLocalConversationServiceConstants.METHOD_updateConversationLocalExtension -> {
                return UpdateConversationLocalExtensionFragment()
            }
            V2NIMLocalConversationServiceConstants.METHOD_getTotalUnreadCount -> {
                return GetTotalUnreadCountFragment()
            }
            V2NIMLocalConversationServiceConstants.METHOD_getUnreadCountByIds -> {
                return GetUnreadCountByIdsFragment()
            }
//            V2NIMLocalConversationServiceConstants.METHOD_getUnreadCountByFilter -> {}
            V2NIMLocalConversationServiceConstants.METHOD_clearTotalUnreadCount -> {
                return ClearTotalUnreadCountFragment()
            }
            V2NIMLocalConversationServiceConstants.METHOD_clearUnreadCountByIds -> {
                return ClearUnreadCountByIdsFragment()
            }
            V2NIMLocalConversationServiceConstants.METHOD_clearUnreadCountByTypes -> {
                return ClearUnreadCountByTypesFragment()
            }
//            V2NIMLocalConversationServiceConstants.METHOD_subscribeUnreadCountByFilter -> {}
//            V2NIMLocalConversationServiceConstants.METHOD_unsubscribeUnreadCountByFilter -> {}
            V2NIMLocalConversationServiceConstants.METHOD_getConversationReadTime -> {
                return GetConversationReadTimeFragment()
            }
            V2NIMLocalConversationServiceConstants.METHOD_markConversationRead -> {
                return MarkConversationReadFragment()
            }
            V2NIMLocalConversationServiceConstants.METHOD_addConversationListener -> {
                return AddConversationListenerFragment()
            }
            V2NIMLocalConversationServiceConstants.METHOD_removeConversationListener -> {
                return RemoveConversationListenerFragment()
            }
            else -> {
                return null
            }
        }
        return null
    }

    override fun addFragment(fragment: String,params: Array<Any?>?) {
        when(fragment){
            V2NIMLocalConversationSelectFragment.NAME -> addFragment(
                V2NIMLocalConversationSelectFragment(),fragment
            )
            V2NIMLocalConversationMultiSelectFragment.NAME -> addFragment(
                V2NIMLocalConversationMultiSelectFragment(),fragment
            )
            else -> {
                //do nothing
            }
        }
    }
}
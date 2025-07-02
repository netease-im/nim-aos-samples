package com.netease.nim.samples.message

import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.base.BaseMethodExecuteActivity
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationMultiSelectFragment
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment
import com.netease.nim.samples.message.common.V2NIMMessageMultiSelectFragment
import com.netease.nim.samples.message.common.V2NIMMessageSelectFragment
import com.netease.nim.samples.message.contants.V2NIMMessageServiceConstants
import com.netease.nim.samples.message.fragment.AddMessageListenerFragment
import com.netease.nim.samples.message.fragment.DeleteMessageFragment
import com.netease.nim.samples.message.fragment.DeleteMessagesFragment
import com.netease.nim.samples.message.fragment.GetMessageListByIdsFragment
import com.netease.nim.samples.message.fragment.GetMessageListByRefersFragment
import com.netease.nim.samples.message.fragment.GetMessageListExFragment
import com.netease.nim.samples.message.fragment.GetMessageListFragment
import com.netease.nim.samples.message.fragment.RemoveMessageListenerFragment
import com.netease.nim.samples.message.fragment.ReplayMessageFragment
import com.netease.nim.samples.message.fragment.RevokeMessageFragment
import com.netease.nim.samples.message.fragment.SendMessageFragment


class V2NIMMessageServiceActivity : BaseMethodExecuteActivity() {


    override fun initFragment(): BaseMethodExecuteFragment<*>? {
        when (methodName) {
            V2NIMMessageServiceConstants.METHOD_sendMessage -> {
                return SendMessageFragment()
            }

            V2NIMMessageServiceConstants.METHOD_replyMessage -> {
                return ReplayMessageFragment()
            }

            V2NIMMessageServiceConstants.METHOD_revokeMessage -> {
                return RevokeMessageFragment()
            }

            V2NIMMessageServiceConstants.METHOD_getMessageList -> {
                return GetMessageListFragment()
            }

            V2NIMMessageServiceConstants.METHOD_getMessageListEx -> {
                return GetMessageListExFragment()
            }

            V2NIMMessageServiceConstants.METHOD_getMessageListByIds -> {
                return GetMessageListByIdsFragment()
            }

            V2NIMMessageServiceConstants.METHOD_getMessageListByRefers -> {
                return GetMessageListByRefersFragment()
            }

            V2NIMMessageServiceConstants.METHOD_deleteMessage -> {
                return DeleteMessageFragment()
            }
            V2NIMMessageServiceConstants.METHOD_deleteMessages -> {
                return DeleteMessagesFragment()
            }
            V2NIMMessageServiceConstants.METHOD_clearHistoryMessage -> {}
            V2NIMMessageServiceConstants.METHOD_updateMessageLocalExtension -> {}
            V2NIMMessageServiceConstants.METHOD_insertMessageToLocal -> {}
            V2NIMMessageServiceConstants.METHOD_pinMessage -> {}
            V2NIMMessageServiceConstants.METHOD_unpinMessage -> {}
            V2NIMMessageServiceConstants.METHOD_updatePinMessage -> {}
            V2NIMMessageServiceConstants.METHOD_getPinnedMessageList -> {}
            V2NIMMessageServiceConstants.METHOD_addQuickComment -> {}
            V2NIMMessageServiceConstants.METHOD_removeQuickComment -> {}
            V2NIMMessageServiceConstants.METHOD_getQuickCommentList -> {}
            V2NIMMessageServiceConstants.METHOD_addCollection -> {}
            V2NIMMessageServiceConstants.METHOD_removeCollections -> {}
            V2NIMMessageServiceConstants.METHOD_updateCollectionExtension -> {}
            V2NIMMessageServiceConstants.METHOD_getCollectionListByOption -> {}
            V2NIMMessageServiceConstants.METHOD_sendP2PMessageReceipt -> {}
            V2NIMMessageServiceConstants.METHOD_getP2PMessageReceipt -> {}
            V2NIMMessageServiceConstants.METHOD_isPeerRead -> {}
            V2NIMMessageServiceConstants.METHOD_sendTeamMessageReceipts -> {}
            V2NIMMessageServiceConstants.METHOD_getTeamMessageReceipts -> {}
            V2NIMMessageServiceConstants.METHOD_getTeamMessageReceiptDetail -> {}
            V2NIMMessageServiceConstants.METHOD_voiceToText -> {}
            V2NIMMessageServiceConstants.METHOD_cancelMessageAttachmentUpload -> {}
            V2NIMMessageServiceConstants.METHOD_searchCloudMessages -> {}
            V2NIMMessageServiceConstants.METHOD_getThreadMessageList -> {}
            V2NIMMessageServiceConstants.METHOD_getLocalThreadMessageList -> {}
            V2NIMMessageServiceConstants.METHOD_modifyMessage -> {}
            V2NIMMessageServiceConstants.METHOD_registerCustomAttachmentParser -> {}
            V2NIMMessageServiceConstants.METHOD_unregisterCustomAttachmentParser -> {}
            V2NIMMessageServiceConstants.METHOD_addMessageListener -> {
                return AddMessageListenerFragment()
            }
            V2NIMMessageServiceConstants.METHOD_removeMessageListener -> {
                return RemoveMessageListenerFragment()
            }
            else -> {
                return null
            }
        }
        return null
    }

    override fun addFragment(fragment: String, params: Array<Any?>?) {
        when (fragment) {
            V2NIMLocalConversationSelectFragment.NAME -> addFragment(
                V2NIMLocalConversationSelectFragment(), fragment
            )

            V2NIMLocalConversationMultiSelectFragment.NAME -> addFragment(
                V2NIMLocalConversationMultiSelectFragment(), fragment
            )

            V2NIMMessageSelectFragment.NAME -> addFragment(
                V2NIMMessageSelectFragment.newInstance(params!![0].toString()), fragment
            )

            V2NIMMessageMultiSelectFragment.NAME -> addFragment(
                V2NIMMessageMultiSelectFragment.newInstance(params!![0].toString()), fragment
            )

            else -> {
                //do nothing
            }
        }
    }
}
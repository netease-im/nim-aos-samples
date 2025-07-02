package com.netease.nim.samples.friend

import com.netease.nim.samples.base.list.model.ItemMultiSelectModel
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation
import com.netease.nimlib.sdk.v2.friend.V2NIMFriendAddApplication

class FriendApplicationItemModel(val application:V2NIMFriendAddApplication) : ItemMultiSelectModel
    ("""
                        applicantAccountId: ${application.applicantAccountId}
                        operatorAccountId: ${application.operatorAccountId}
                        recipientAccountId: ${application.recipientAccountId}
                         status: ${application.status.name}
                         isRead: ${application.isRead}
                        """.trimIndent(), null) {
}
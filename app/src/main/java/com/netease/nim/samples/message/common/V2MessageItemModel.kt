package com.netease.nim.samples.message.common

import com.netease.nim.samples.base.list.model.ItemMultiSelectModel
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation
import com.netease.nimlib.sdk.v2.message.V2NIMMessage

class V2MessageItemModel(val message: V2NIMMessage) : ItemMultiSelectModel("""
                        text: ${message.text}
                        clientId: ${message.messageClientId}
                        conversationId: ${message.conversationId} messageType: ${message.messageType}
                        attachment: ${message.attachment?.raw}
                        """.trimIndent(), null) {

}
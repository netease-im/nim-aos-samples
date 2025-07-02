package com.netease.nim.samples.localconversation.common

import com.netease.nim.samples.base.list.model.ItemMultiSelectModel
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation

class V2LocalConversationItemModel(val conversation:V2NIMLocalConversation) : ItemMultiSelectModel("""
                        name: ${conversation.name}
                        id: ${conversation.conversationId} type: ${conversation.type}
                        """.trimIndent(), null) {
}
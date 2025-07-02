package com.netease.nim.samples.common

import com.netease.nimlib.sdk.v2.message.V2NIMMessage
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer
import com.netease.nimlib.sdk.v2.message.result.V2NIMSendMessageResult

fun getV2NIMMessageJson(message: V2NIMMessage?):String {
    if (message == null) {
        return "null"
    }
    return """{
            messageClientId = ${message.messageClientId},
            messageServerId = ${message.messageServerId},
            createdTime = ${message.createTime},
            messageType = ${message.messageType},
            senderId  = ${message.senderId},
            receiverId = ${message.receiverId},
            conversationType = ${message.conversationType},
            conversationId = ${message.conversationId},
            subType = ${message.subType},
            text = ${message.text},
            messageStatus = ${message.messageStatus},
            attachment = ${message.attachment?.raw},
            threadRoot = ${getV2NIMMessageReferJson(message.threadRoot)},
            threadReply = ${getV2NIMMessageReferJson(message.threadReply)},
            }
        """.trimIndent()
}

fun getV2NIMMessageListJson(messageList: List<V2NIMMessage>?):String {
    if (messageList == null) {
        return "null"
    }
    val sb = StringBuilder()
    for (message in messageList) {
        sb.append(getV2NIMMessageJson(message))
        sb.append("\n") // 换行符，方便查看结果。实际使用时可以去掉。
    }
    return """{
        ${sb.toString()}
        }""".trimIndent()
}



fun getV2NIMSendMessageResultJson(result: V2NIMSendMessageResult?): String {
    if (result == null) {
        return "null"
    }
    return """{
            message = ${getV2NIMMessageJson(result.message)},
            antispamResult = ${result.antispamResult},
            clientAntispamResult = ${result.clientAntispamResult},
            }
        """.trimIndent()
}

fun getV2NIMMessageReferJson(message: V2NIMMessageRefer?):String {
    if (message == null) {
        return "null"
    }
    return """{
            messageClientId = ${message.messageClientId},
            messageServerId = ${message.messageServerId},
            createdTime = ${message.createTime},
            senderId  = ${message.senderId},
            receiverId = ${message.receiverId},
            conversationType = ${message.conversationType},
            conversationId = ${message.conversationId},
            }
        """.trimIndent()
}
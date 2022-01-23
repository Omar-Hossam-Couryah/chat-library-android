package com.couryah.firebase_chat.models

import com.couryah.firebase_chat.ChatConstants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.Expose

@IgnoreExtraProperties
class ChatModel(
    var messageId: String,
    @Expose var senderId: String,
    @Expose var receiverId: String,
    @Expose var message: String,
    @Expose var time: Timestamp,
    @Expose var type: String,
    var progress: Double = 0.0,
    @Expose var uri: String? = null,
    @Expose var messageStatus: String? = MessageStatus.NOT_SENT.name
) {
    enum class MessageType {
        TEXT, IMAGE
    }

    enum class MessageStatus {
        NOT_SENT, SENT, RECEIVED
    }

    override fun equals(other: Any?): Boolean {
        return time == (other as ChatModel).time
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun hasBeenSeen(): Boolean {
        return messageStatus == MessageStatus.RECEIVED.name
    }

    fun hasBeenSent(): Boolean {
        return messageStatus == MessageStatus.SENT.name
    }

    fun getHashMap(): HashMap<String, Any?> {
        return hashMapOf(ChatConstants.SENDER_ID to senderId,
                        ChatConstants.RECEIVER_ID to receiverId,
                        ChatConstants.MESSAGE to message,
                        ChatConstants.TIME to time,
                        ChatConstants.TYPE to type,
                        ChatConstants.URI to uri,
                        ChatConstants.MESSAGE_STATUS to messageStatus)
    }
}
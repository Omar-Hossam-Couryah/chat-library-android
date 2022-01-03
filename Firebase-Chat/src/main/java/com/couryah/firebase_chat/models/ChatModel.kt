package com.couryah.firebase_chat.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.Expose

@IgnoreExtraProperties
class ChatModel(
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
}
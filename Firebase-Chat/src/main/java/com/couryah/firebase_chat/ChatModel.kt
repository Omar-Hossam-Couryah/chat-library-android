package com.couryah.firebase_chat

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
    var uri: String? = null
) {
    enum class MessageType {
        TEXT, IMAGE
    }

    override fun equals(other: Any?): Boolean {
        return time == (other as ChatModel).time
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
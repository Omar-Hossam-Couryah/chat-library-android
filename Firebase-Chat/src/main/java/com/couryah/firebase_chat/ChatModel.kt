package com.couryah.firebase_chat

import com.google.firebase.Timestamp
import com.google.gson.annotations.Expose

class ChatModel(
    @Expose var senderId: String,
    @Expose var receiverId: String,
    @Expose var message: String,
    @Expose var time: Timestamp
)
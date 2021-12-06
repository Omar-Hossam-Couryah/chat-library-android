package com.couryah.firebase_chat

import com.google.gson.annotations.Expose
import java.util.*

class ChatModel(@Expose var senderId: String, @Expose var receiverId: String, @Expose var message: String, @Expose var time: Date)
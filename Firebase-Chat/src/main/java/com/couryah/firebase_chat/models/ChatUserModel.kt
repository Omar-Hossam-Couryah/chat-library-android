package com.couryah.firebase_chat.models

import com.google.gson.annotations.Expose
import java.io.Serializable

class ChatUserModel: Serializable {
    var id = ""
    var name = ""
    var firebaseNotificationToken = ""
    var isSender = false
}
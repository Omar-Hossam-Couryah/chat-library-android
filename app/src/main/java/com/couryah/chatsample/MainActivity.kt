package com.couryah.chatsample

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.couryah.firebase_chat.MainChatActivity
import com.couryah.firebase_chat.models.ChatUserModel
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        val customerButton = findViewById<Button>(R.id.customer_button)
        customerButton.setOnClickListener {
            startChat(false)
        }

        val shopperButton = findViewById<Button>(R.id.shopper_button)
        shopperButton.setOnClickListener {
            startChat(true)
        }
    }

    private fun startChat(isShopper: Boolean) {
        val user1 = ChatUserModel()
        user1.id = "CustomerId0101010101010"
        user1.isSender = !isShopper
        user1.firebaseNotificationToken = ""
        user1.name = "Omar"

        val user2 = ChatUserModel()
        user2.id = "ShopperId01010101010101"
        user2.isSender = isShopper
        user2.firebaseNotificationToken = ""
        user2.name = "Ahmed"


        MainChatActivity.openChat(this, user1, user2, "8200")
    }
}
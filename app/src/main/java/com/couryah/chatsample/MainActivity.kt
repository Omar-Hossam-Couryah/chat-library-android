package com.couryah.chatsample

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.couryah.firebase_chat.MainChatActivity
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        val button = findViewById<Button>(R.id.open_button)
        button.setOnClickListener {
            startChat()
        }
    }

    private fun startChat() {
        MainChatActivity.openChat(this, "CustomerId0101010101010", "ShopperId01010101010101", false)
    }
}
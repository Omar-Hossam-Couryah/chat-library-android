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
        MainChatActivity.openChat(this, "CustomerId0101010101010", "ShopperId01010101010101", isShopper)
    }
}
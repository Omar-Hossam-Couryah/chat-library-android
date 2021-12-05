package com.couryah.chatsample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.couryah.firebase_chat.MainChatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.open_button)
        button.setOnClickListener {
            startChat()
        }
    }

    private fun startChat() {
        val intent = Intent(this, MainChatActivity::class.java)
        startActivity(intent)
    }
}
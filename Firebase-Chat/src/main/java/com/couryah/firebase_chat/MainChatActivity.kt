package com.couryah.firebase_chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainChatActivity : AppCompatActivity() {

    private lateinit var messageEditText: EditText
    private lateinit var customerId: String
    private lateinit var shopperId: String
    private var isShopper = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_chat)
        getDataFromIntent()
        messageEditText = findViewById(R.id.send_edit_text)
        initSendButton()
    }

    private fun getDataFromIntent() {
        if (intent.hasExtra(CUSTOMER_ID)) {
            customerId = intent.getStringExtra(CUSTOMER_ID)!!
            shopperId = intent.getStringExtra(SHOPPER_ID)!!
            isShopper = intent.getBooleanExtra(IS_SHOPPER, false)
        }
    }

    private fun initSendButton() {
        val sendButton = findViewById<ImageButton>(R.id.send_button)
        sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        if (messageEditText.text.isNotEmpty()) {
            FirebaseRepository().sendMessage(createMessage(), "$customerId-$shopperId")
        }
    }

    private fun createMessage(): ChatModel {
        return if (isShopper) {
            ChatModel(shopperId, customerId, messageEditText.text.toString(), Date())
        } else {
            ChatModel(customerId, shopperId, messageEditText.text.toString(), Date())
        }
    }

    companion object {
        private const val CUSTOMER_ID = "CustomerId"
        private const val SHOPPER_ID = "shopperId"
        private const val IS_SHOPPER = "isShopper"

        fun openChat(context: Context, customerId: String, shopperId: String, isShopper: Boolean) {
            val intent = Intent(context, MainChatActivity::class.java)
            intent.putExtra(CUSTOMER_ID, customerId)
            intent.putExtra(SHOPPER_ID, shopperId)
            intent.putExtra(IS_SHOPPER, isShopper)
            context.startActivity(intent)
        }
    }
}
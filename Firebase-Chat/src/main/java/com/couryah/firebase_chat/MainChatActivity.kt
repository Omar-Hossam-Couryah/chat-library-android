package com.couryah.firebase_chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp

class MainChatActivity : AppCompatActivity() {

    private lateinit var messageEditText: EditText
    private lateinit var chatRecyclerView: RecyclerView

    private lateinit var customerId: String
    private lateinit var shopperId: String
    private var isShopper = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_chat)
        getDataFromIntent()
        messageEditText = findViewById(R.id.send_edit_text)
        chatRecyclerView = findViewById(R.id.chat_recyclerview)
        initSendButton()
        loadMessages()
    }

    private fun getDataFromIntent() {
        if (intent.hasExtra(CUSTOMER_ID)) {
            customerId = intent.getStringExtra(CUSTOMER_ID)!!
            shopperId = intent.getStringExtra(SHOPPER_ID)!!
            isShopper = intent.getBooleanExtra(IS_SHOPPER, false)
        }
    }

    private fun loadMessages() {
        FirebaseRepository().getMessages("$customerId-$shopperId") { chatList, error ->
            if (error == null) {
                val chatAdapter = ChatAdapter(if (isShopper) shopperId else customerId)
                chatRecyclerView.adapter = chatAdapter
                chatList?.reverse()
                chatAdapter.updateChatList(chatList!!)
            } else {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initSendButton() {
        val sendButton = findViewById<ImageButton>(R.id.send_button)
        sendButton.setOnClickListener {
            sendTextMessage()
        }
    }

    private fun sendTextMessage() {
        if (messageEditText.text.isNotEmpty()) {
            FirebaseRepository().sendMessage(createMessage(messageEditText.text.toString(), ChatModel.MessageType.TEXT.name), "$customerId-$shopperId")
            messageEditText.setText("")
        }
    }

    private fun sendImageMessage() {
        FirebaseRepository().sendMessage(createMessage("", ChatModel.MessageType.IMAGE.name), "$customerId-$shopperId")
    }

    private fun createMessage(text: String, messageType: String): ChatModel {
        return if (isShopper) {
            ChatModel(shopperId, customerId, text, Timestamp.now(), messageType)
        } else {
            ChatModel(customerId, shopperId, text, Timestamp.now(), messageType)
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
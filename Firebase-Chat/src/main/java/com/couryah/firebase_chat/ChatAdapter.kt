package com.couryah.firebase_chat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatAdapter(private val senderId: String) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var chatList = ArrayList<ChatModel>()
    private var simpleDateFormat = SimpleDateFormat("h:m a", Locale.getDefault())
    class ChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var chatTextView: TextView = itemView.findViewById(R.id.chat_bubble)
        var timeTextView: TextView = itemView.findViewById(R.id.time_textview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return when (viewType) {
            SENDER_VIEW_TYPE -> ChatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.sender_chat_bubble, parent, false))
            else -> {
                    ChatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.receiver_chat_bubble, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatModel = chatList[position]
        holder.chatTextView.text = chatModel.message
        holder.timeTextView.text = simpleDateFormat.format(chatModel.time.toDate())
    }

    fun updateChatList(chatList: ArrayList<ChatModel>) {
        this.chatList = chatList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val chatModel = chatList[position]
        return if (chatModel.senderId == senderId) {
            Log.d("Chat ViewType", "ViewType = Sender")
            SENDER_VIEW_TYPE
        } else {
            Log.d("Chat ViewType", "ViewType = Receiver")
            RECEIVER_VIEW_TYPE
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    companion object {
        private const val SENDER_VIEW_TYPE = 0
        private const val RECEIVER_VIEW_TYPE = 1
    }
}
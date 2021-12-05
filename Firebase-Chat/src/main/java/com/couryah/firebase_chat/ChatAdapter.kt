package com.couryah.firebase_chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(var chatList: List<ChatModel>): RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var chatTextView: TextView = itemView.findViewById(R.id.sender_chat_bubble)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.sender_chat_bubble, parent, false))
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatModel = chatList[position]
        holder.chatTextView.text = chatModel.message
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
}
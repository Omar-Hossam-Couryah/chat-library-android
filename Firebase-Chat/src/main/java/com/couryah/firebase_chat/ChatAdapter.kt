package com.couryah.firebase_chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatAdapter(private val senderId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var chatList = ArrayList<ChatModel>()

    open class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var timeTextView: TextView = itemView.findViewById(R.id.time_textview)
        private var simpleDateFormat = SimpleDateFormat("h:m a", Locale.getDefault())

        open fun bind(chatModel: ChatModel) {
            timeTextView.text = simpleDateFormat.format(chatModel.time.toDate())
        }
    }

    class TextViewHolder(itemView: View) : ChatViewHolder(itemView) {
        private var chatTextView: TextView = itemView.findViewById(R.id.chat_bubble)

        override fun bind(chatModel: ChatModel) {
            super.bind(chatModel)
            chatTextView.text = chatModel.message
        }
    }

    class ImageViewHolder(itemView: View) : ChatViewHolder(itemView) {
        var chatImageView: ImageView = itemView.findViewById(R.id.chat_image)

        override fun bind(chatModel: ChatModel) {
            super.bind(chatModel)
            Glide.with(itemView.context).load(chatModel.message).into(chatImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            SENDER_CHAT_VIEW_TYPE -> TextViewHolder(
                layoutInflater.inflate(
                    R.layout.sender_chat_bubble,
                    parent,
                    false
                )
            )
            RECEIVER_CHAT_VIEW_TYPE -> TextViewHolder(
                layoutInflater.inflate(
                    R.layout.receiver_chat_bubble,
                    parent,
                    false
                )
            )
            SENDER_IMAGE_VIEW_TYPE -> ImageViewHolder(
                layoutInflater.inflate(
                    R.layout.sender_image_bubble,
                    parent,
                    false
                )
            )
            else -> ImageViewHolder(
                layoutInflater.inflate(
                    R.layout.receiver_image_bubble,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatModel = chatList[position]
        val viewHolder = holder as ChatViewHolder
        viewHolder.bind(chatModel)
    }

    fun updateChatList(chatList: ArrayList<ChatModel>) {
        this.chatList = chatList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val chatModel = chatList[position]
        return if (chatModel.senderId == senderId) {
            if (chatModel.type == ChatModel.MessageType.TEXT.name) {
                SENDER_CHAT_VIEW_TYPE
            } else {
                SENDER_IMAGE_VIEW_TYPE
            }
        } else {
            if (chatModel.type == ChatModel.MessageType.IMAGE.name) {
                RECEIVER_CHAT_VIEW_TYPE
            } else {
                RECEIVER_IMAGE_VIEW_TYPE
            }
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    companion object {
        private const val SENDER_CHAT_VIEW_TYPE = 0
        private const val RECEIVER_CHAT_VIEW_TYPE = 1
        private const val SENDER_IMAGE_VIEW_TYPE = 2
        private const val RECEIVER_IMAGE_VIEW_TYPE = 3
    }
}
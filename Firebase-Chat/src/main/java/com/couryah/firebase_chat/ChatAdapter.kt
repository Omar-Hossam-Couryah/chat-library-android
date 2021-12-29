package com.couryah.firebase_chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.couryah.firebase_chat.activities.ImageActivity
import com.couryah.firebase_chat.models.ChatModel
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatAdapter(private val applicationContext: Context, private val senderId: String,
                    private val onImageClicked: (String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var chatList = ArrayList<ChatModel>()

    open class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var timeTextView: TextView = itemView.findViewById(R.id.time_textview)
        private var simpleDateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        open fun bind(applicationContext: Context, chatModel: ChatModel, senderId: String, onImageClicked: (String) -> Unit) {
            timeTextView.text = simpleDateFormat.format(chatModel.time.toDate())
        }
    }

    class TextViewHolder(itemView: View) : ChatViewHolder(itemView) {
        private var chatTextView: TextView = itemView.findViewById(R.id.chat_bubble)

        override fun bind(applicationContext: Context, chatModel: ChatModel, senderId: String, onImageClicked: (String) -> Unit) {
            super.bind(applicationContext, chatModel, senderId, onImageClicked)
            chatTextView.text = chatModel.message
        }
    }

    class ImageViewHolder(itemView: View) : ChatViewHolder(itemView) {
        private var chatImageView: ImageView = itemView.findViewById(R.id.chat_image)
        override fun bind(applicationContext: Context, chatModel: ChatModel, senderId: String, onImageClicked: (String) -> Unit) {
            super.bind(applicationContext, chatModel, senderId, onImageClicked)

            val circularProgressDrawable = CircularProgressDrawable(itemView.context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            val imageLink: String = if (chatModel.uri != null && chatModel.senderId == senderId) {
                chatModel.uri.toString()
            } else {
                chatModel.message
            }
            Glide.with(applicationContext).load(imageLink)
                .placeholder(circularProgressDrawable).into(chatImageView)

            chatImageView.setOnClickListener {
                onImageClicked(imageLink)
            }

            if (chatModel.progress >= 100) {
                progressBar.isVisible = false
            } else {
                progressBar.isVisible = true
                progressBar.progress = chatModel.progress.toInt()
            }
        }

        private var progressBar: CircularProgressIndicator =
            itemView.findViewById(R.id.progress_bar)
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
        viewHolder.bind(applicationContext, chatModel, senderId, onImageClicked)
    }

    fun updateChatList(chatList: ArrayList<ChatModel>) {
        this.chatList = chatList
        notifyDataSetChanged()
    }

    fun addDummyChatMessage(chatModel: ChatModel) {
        chatList.add(chatModel)
        notifyItemInserted(chatList.size - 1)
    }

    fun updateProgress(chatModel: ChatModel) {
        val index = chatList.indexOf(chatModel)
        if (index > 0 && index < chatList.size) {
            notifyItemChanged(index)
        }
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
            if (chatModel.type == ChatModel.MessageType.TEXT.name) {
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
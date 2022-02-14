package com.couryah.firebase_chat

import android.net.Uri
import android.util.Log
import com.couryah.firebase_chat.models.ChatModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class FirebaseRepository {

    private val fireStore = Firebase.firestore
    private val fireStorage = FirebaseStorage.getInstance().reference

    fun getMessages(roomId: String, onFinish: (ArrayList<ChatModel>?, String?) -> Unit) {
        fireStore.collection(ChatConstants.ChatMessagesKey).document(ChatConstants.REPLACEMENT_CHAT)
            .collection(roomId)
            .addSnapshotListener { snapshot, e ->
                var contactUsMessages = arrayListOf<ChatModel>()
                if (snapshot != null) {
                    val messages = snapshot.documents
                    for (messageOb in messages) {
                        contactUsMessages.add(
                            ChatModel(
                                messageOb.id,
                                messageOb[ChatConstants.SENDER_ID] as String,
                                messageOb[ChatConstants.RECEIVER_ID] as String,
                                messageOb[ChatConstants.MESSAGE] as String,
                                messageOb[ChatConstants.TIME] as Timestamp,
                                messageOb[ChatConstants.TYPE] as String,
                                messageStatus = messageOb[ChatConstants.MESSAGE_STATUS] as String?
                            )
                        )
                    }
                    contactUsMessages = ArrayList(contactUsMessages.sortedBy { it.time })
                }
                onFinish(contactUsMessages, null)
            }
    }

    fun sendMessage(chatModel: ChatModel, roomId: String) {
        chatModel.messageStatus = ChatModel.MessageStatus.SENT.name
        fireStore.collection(ChatConstants.ChatMessagesKey)
            .document(ChatConstants.REPLACEMENT_CHAT)
            .collection(roomId)
            .document().set(chatModel.getHashMap())
            .addOnFailureListener {
                fireStore.collection(ChatConstants.ChatMessagesKey)
                    .document(ChatConstants.REPLACEMENT_CHAT).collection(roomId)
                    .document().set(chatModel.getHashMap()).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("Add Task", "Add Task Succeeded")
                        } else {
                            Log.d("Add Task", "Add Task Failed")
                        }
                    }
            }
    }

    fun updateSeenStatus(chatList: List<ChatModel>, roomId: String) {
        for (chatModel in chatList) {
            fireStore.collection(ChatConstants.ChatMessagesKey).document(ChatConstants.REPLACEMENT_CHAT)
                .collection(roomId).document(chatModel.messageId).update(ChatConstants.MESSAGE_STATUS, chatModel.messageStatus)
        }
    }

    fun saveImage(
        id: String,
        uri: Uri,
        onProgress: (Double) -> Unit,
        onFinish: (String?, String?) -> Unit
    ) {
        val fileReference = fireStorage.child(ChatConstants.CHAT_IMAGES).child("$id.jpg")
        fileReference.putFile(uri).addOnProgressListener {
            onProgress((it.bytesTransferred * 100.0) / it.totalByteCount)
        }.addOnSuccessListener {
            fileReference.downloadUrl.addOnSuccessListener {
                onFinish(it.toString(), null)
            }
        }
            .addOnFailureListener {
                onFinish(null, it.localizedMessage)
            }
    }
}
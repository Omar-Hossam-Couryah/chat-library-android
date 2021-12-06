package com.couryah.firebase_chat

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class FirebaseRepository {

    private val fireStore = Firebase.firestore
    fun getMessages(roomId: String, onFinish: (List<ChatModel>?, String?) -> Unit) {
        fireStore.collection(ChatConstants.ChatMessagesKey).document(roomId)
            .addSnapshotListener { snapshot, e ->
                var contactUsMessages = arrayListOf<ChatModel>()
                if (snapshot != null && snapshot.exists()) {
                    Log.d("TAG", "Current data: ${snapshot.data}")
                    val messages = snapshot.get(ChatConstants.ChatMessagesKey) as List<Any>
                    for (message in messages) {
                        val messageOb = message as HashMap<String, Any>
                        contactUsMessages.add(
                            ChatModel(
                                messageOb["senderId"] as String,
                                messageOb["receiverId"] as String,
                                messageOb["message"] as String,
                                messageOb["time"] as Date,
                            )
                        )
                    }
                    contactUsMessages = ArrayList(contactUsMessages.sortedBy { it.time })
                }
                onFinish(contactUsMessages, null)
            }
    }

    fun sendMessage(chatModel: ChatModel, roomId: String) {
        fireStore.collection(ChatConstants.ChatMessagesKey)
            .document(roomId)
            .update(ChatConstants.ChatMessagesKey, FieldValue.arrayUnion(chatModel))
            .addOnFailureListener {
                val messages = listOf(chatModel)
                val docData = hashMapOf(ChatConstants.ChatMessagesKey to messages)
                Firebase.firestore.collection(ChatConstants.ChatMessagesKey)
                    .document(roomId).set(docData)
            }
    }
}
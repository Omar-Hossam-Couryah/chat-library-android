package com.couryah.firebase_chat

import android.net.Uri
import android.util.Log
import com.couryah.firebase_chat.models.ChatModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList

class FirebaseRepository {

    private val fireStore = Firebase.firestore
    private val fireStorage = FirebaseStorage.getInstance().reference

    fun getMessages(roomId: String, onFinish: (ArrayList<ChatModel>?, String?) -> Unit) {
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
                                messageOb["time"] as Timestamp,
                                messageOb["type"] as String,
                                uri = messageOb["uri"] as String?
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

    fun saveImage(
        id: String,
        uri: Uri,
        onProgress: (Double) -> Unit,
        onFinish: (String?, String?) -> Unit
    ) {
        val fileReference = fireStorage.child("ChatImages").child("$id.jpg")
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
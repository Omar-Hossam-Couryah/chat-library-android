package com.couryah.firebase_chat.activities

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.couryah.firebase_chat.*
import com.couryah.firebase_chat.ChatConstants.CUSTOMER_ID
import com.couryah.firebase_chat.ChatConstants.ORDER_ID
import com.couryah.firebase_chat.ChatConstants.SHOPPER_ID
import com.couryah.firebase_chat.models.ChatModel
import com.couryah.firebase_chat.models.ChatUserModel
import com.google.firebase.Timestamp
import java.util.*


class MainChatActivity : AppCompatActivity() {

    private lateinit var messageEditText: EditText
    private lateinit var chatRecyclerView: RecyclerView

    private lateinit var user1: ChatUserModel
    private lateinit var user2: ChatUserModel
    private lateinit var orderId: String

    private var imageUri: Uri? = null

    private lateinit var chatAdapter: ChatAdapter

    private lateinit var takePicture: ActivityResultLauncher<Uri>
    private lateinit var getImageFromGallery: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_chat)
        getDataFromIntent()
        initializeViews()
        initButtons()
        registerActivities()
        loadMessages()
    }

    private fun registerActivities() {
        takePicture =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
                if (success) {
                    // The image was saved into the given Uri -> do something with it
                    uploadImage()
                }
            }

        getImageFromGallery = registerForActivityResult(GetContent()) { uri ->
            if (uri != null) {
                imageUri = uri
                uploadImage()
            }
        }
    }

    private fun initializeViews() {
        messageEditText = findViewById(R.id.send_edit_text)
        chatRecyclerView = findViewById(R.id.chat_recyclerview)
        chatRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->

            if (bottom < oldBottom) {
                scrollToStart()
            }
        }
        chatRecyclerView.adapter = chatAdapter

        val screenTitle = findViewById<TextView>(R.id.screen_title)
        screenTitle.text = getString(R.string.order_no, orderId)
    }

    private fun getDataFromIntent() {
        if (intent.hasExtra(CUSTOMER_ID)) {
            user1 = (intent.getSerializableExtra(CUSTOMER_ID) as ChatUserModel?)!!
            user2 = (intent.getSerializableExtra(SHOPPER_ID) as ChatUserModel?)!!
            orderId = intent.getStringExtra(ORDER_ID)!!
            chatAdapter = ChatAdapter(applicationContext, if (user2.isSender) user2.id else user1.id) {
                ImageActivity.open(this, orderId, it)
            }
        }
    }

    private fun loadMessages() {
        FirebaseRepository().getMessages(orderId) { chatList, error ->
            if (error == null) {
                hideNoMessagesLayout(chatList?.isEmpty()!!)
                chatAdapter.updateChatList(chatList)
                setMessagesAsSeen(chatList)
                scrollToStart()
            } else {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun hideNoMessagesLayout(show: Boolean) {
        val noChatView = findViewById<LinearLayout>(R.id.no_chat_container)
        noChatView.isVisible = show
    }

    private fun setMessagesAsSeen(chatList: List<ChatModel>) {
        val messagesToUpdate = arrayListOf<ChatModel>()
        for (chatModel in chatList) {
            val senderId = if (user1.isSender) user1.id else user2.id
            if (chatModel.senderId != senderId && !chatModel.hasBeenSeen()) {
                chatModel.messageStatus = ChatModel.MessageStatus.RECEIVED.name
                messagesToUpdate.add(chatModel)
            }
        }

        FirebaseRepository().updateSeenStatus(messagesToUpdate, orderId)
    }

    private fun initButtons() {
        val sendButton = findViewById<ImageButton>(R.id.send_button)
        sendButton.setOnClickListener {
            sendTextMessage()
        }

        val imageButton = findViewById<ImageButton>(R.id.image_button)
        imageButton.setColorFilter(ContextCompat.getColor(this, R.color.chat_primary))
        imageButton.setOnClickListener {
            onImageSourceClicked()
        }

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener { finish() }
    }

    private fun sendTextMessage() {
        if (messageEditText.text.isNotEmpty()) {
            val chatMessage = createMessage(
                messageEditText.text.toString(),
                ChatModel.MessageType.TEXT.name
            )
            chatAdapter.addDummyChatMessage(chatMessage)
            scrollToStart()
            hideNoMessagesLayout(false)
            FirebaseRepository().sendMessage(
                chatMessage, orderId
            )
            messageEditText.setText("")
        }
    }

    private fun scrollToStart() {
        chatRecyclerView.postDelayed({
            with(chatRecyclerView) {
                if (chatAdapter.itemCount > 0) {
                    Log.d("Scrolling", "Smooth Scroll: ${chatAdapter.itemCount - 1}")
                    smoothScrollToPosition(
                        chatAdapter.itemCount - 1
                    )
                }
            }
        }, 100)
    }

    private fun sendImageMessage(message: ChatModel) {
        FirebaseRepository().sendMessage(
            message,
            orderId
        )
    }

    private fun createMessage(text: String, messageType: String): ChatModel {
        val senderId = if (user2.isSender) user2.id else user1.id
        val receiverId = if (user2.isSender) user1.id else user2.id
        return ChatModel("", senderId, receiverId, text, Timestamp.now(), messageType)
    }

    private fun onImageSourceClicked() {
        UIUtils.showCameraGalleryDialog(this)
        { openCamera ->
            if (openCamera) {
                openCamera()
            } else {
                imageUri = null
                getImageFromGallery.launch("image/*")
            }
        }
    }

    private fun openCamera() {
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ), ChatConstants.CameraPermissionCode
            )
        } else {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
            imageUri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            )
            takePicture.launch(imageUri)
        }
    }

    private fun uploadImage() {
        val message = createMessage("", ChatModel.MessageType.IMAGE.name)
        chatAdapter.addDummyChatMessage(message)
        scrollToStart()
        FirebaseRepository().saveImage(
            "$orderId-${Date()}",
            imageUri!!, {
                message.progress = it
                chatAdapter.updateProgress(message)
            }) { downloadLink, error ->
            if (error == null) {
                message.message = downloadLink!!
                sendImageMessage(message)
            } else {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ChatConstants.CameraPermissionCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {

        fun openChat(context: Context, user1: ChatUserModel, user2: ChatUserModel, orderId: String) {
            val intent = Intent(context, MainChatActivity::class.java)
            intent.putExtra(CUSTOMER_ID, user1)
            intent.putExtra(SHOPPER_ID, user2)
            intent.putExtra(ORDER_ID, orderId)
            context.startActivity(intent)
        }
    }
}
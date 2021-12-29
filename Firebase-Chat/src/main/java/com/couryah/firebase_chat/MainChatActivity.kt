package com.couryah.firebase_chat

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import java.util.*


class MainChatActivity : AppCompatActivity() {

    private lateinit var messageEditText: EditText
    private lateinit var chatRecyclerView: RecyclerView

    private lateinit var customerId: String
    private lateinit var shopperId: String
    private var isShopper = false

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
            imageUri = uri
            uploadImage()
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
    }

    private fun getDataFromIntent() {
        if (intent.hasExtra(CUSTOMER_ID)) {
            customerId = intent.getStringExtra(CUSTOMER_ID)!!
            shopperId = intent.getStringExtra(SHOPPER_ID)!!
            isShopper = intent.getBooleanExtra(IS_SHOPPER, false)
            chatAdapter = ChatAdapter(if (isShopper) shopperId else customerId)
        }
    }

    private fun loadMessages() {
        FirebaseRepository().getMessages("$customerId-$shopperId") { chatList, error ->
            if (error == null) {
                val noChatView = findViewById<LinearLayout>(R.id.no_chat_container)
                noChatView.isVisible = chatList?.isEmpty()!!
                chatAdapter.updateChatList(chatList)
                scrollToStart()
            } else {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
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
                ChatModel.MessageType.TEXT.name, null
            )
            chatAdapter.addDummyChatMessage(chatMessage)
            scrollToStart()
            FirebaseRepository().sendMessage(
                chatMessage, "$customerId-$shopperId"
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
            "$customerId-$shopperId"
        )
    }

    private fun createMessage(text: String, messageType: String, imageUri: String?): ChatModel {
        val chatModel = if (isShopper) {
            ChatModel(shopperId, customerId, text, Timestamp.now(), messageType, uri = imageUri)
        } else {
            ChatModel(customerId, shopperId, text, Timestamp.now(), messageType, uri = imageUri)
        }
        return chatModel
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
        val message = createMessage("", ChatModel.MessageType.IMAGE.name, imageUri.toString())
        chatAdapter.addDummyChatMessage(message)
        scrollToStart()
        FirebaseRepository().saveImage(
            "$customerId-$shopperId-${Date()}",
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
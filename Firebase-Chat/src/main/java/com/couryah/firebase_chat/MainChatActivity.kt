package com.couryah.firebase_chat

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import java.io.FileNotFoundException
import java.util.*

class MainChatActivity : AppCompatActivity() {

    private lateinit var messageEditText: EditText
    private lateinit var chatRecyclerView: RecyclerView

    private lateinit var customerId: String
    private lateinit var shopperId: String
    private var isShopper = false

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_chat)
        getDataFromIntent()
        messageEditText = findViewById(R.id.send_edit_text)
        chatRecyclerView = findViewById(R.id.chat_recyclerview)
        initButtons()
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

    private fun initButtons() {
        val sendButton = findViewById<ImageButton>(R.id.send_button)
        sendButton.setOnClickListener {
            sendTextMessage()
        }

        val imageButton = findViewById<ImageButton>(R.id.image_button)
        imageButton.setOnClickListener {
            onImageSourceClicked()
        }
    }

    private fun sendTextMessage() {
        if (messageEditText.text.isNotEmpty()) {
            FirebaseRepository().sendMessage(
                createMessage(
                    messageEditText.text.toString(),
                    ChatModel.MessageType.TEXT.name
                ), "$customerId-$shopperId"
            )
            messageEditText.setText("")
        }
    }

    private fun sendImageMessage(imageLink: String) {
        FirebaseRepository().sendMessage(
            createMessage(imageLink, ChatModel.MessageType.IMAGE.name),
            "$customerId-$shopperId"
        )
    }

    private fun createMessage(text: String, messageType: String): ChatModel {
        return if (isShopper) {
            ChatModel(shopperId, customerId, text, Timestamp.now(), messageType)
        } else {
            ChatModel(customerId, shopperId, text, Timestamp.now(), messageType)
        }
    }

    private fun onImageSourceClicked() {
        UIUtils.showCameraGalleryDialog(this)
        { openCamera ->
            if (openCamera) {
                openCamera()
            } else {
                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                startActivityForResult(photoPickerIntent, ChatConstants.GalleryRequestCode)
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
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(intent, ChatConstants.CameraRequestCode)
        }
    }

    private fun uploadImage() {
        FirebaseRepository().saveImage("$customerId-$shopperId-${Date()}", imageUri!!) { downloadLink, error ->
            if (error == null) {
                sendImageMessage(downloadLink!!)
            } else {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == ChatConstants.GalleryRequestCode) {
                try {
                    imageUri = data?.data
//                    val imageStream: InputStream? =
//                        this.contentResolver.openInputStream(viewModel.selectedImage.value!!)
//                    val selectedImage = BitmapFactory.decodeStream(imageStream)
//                    mBinding.gameImageView.setImageBitmap(selectedImage)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                }
            } else if (requestCode == ChatConstants.CameraRequestCode) {

            }
            uploadImage()
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
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, ChatConstants.CameraRequestCode)
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
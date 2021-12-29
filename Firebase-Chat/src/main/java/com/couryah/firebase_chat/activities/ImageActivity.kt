package com.couryah.firebase_chat.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.couryah.firebase_chat.ChatConstants
import com.couryah.firebase_chat.R

class ImageActivity : AppCompatActivity() {

    private lateinit var orderId: String
    private lateinit var imageLink: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        getDataFromIntent()
        initViews()
    }

    private fun initViews() {
        val screenTitle = findViewById<TextView>(R.id.screen_title)
        screenTitle.text = getString(R.string.order_no, orderId)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener { finish() }

        Glide.with(applicationContext).load(imageLink).into(findViewById(R.id.imageview))
    }

    private fun getDataFromIntent() {
        if (intent.hasExtra(ChatConstants.IMAGE_LINK)) {
            orderId = intent.getStringExtra(ChatConstants.ORDER_ID)!!
            imageLink = intent.getStringExtra(ChatConstants.IMAGE_LINK)!!
        }
    }

    companion object {
        fun open(context: Context, orderId: String, imageLink: String) {
            val intent = Intent(context, ImageActivity::class.java)
            intent.putExtra(ChatConstants.ORDER_ID, orderId)
            intent.putExtra(ChatConstants.IMAGE_LINK, imageLink)
            context.startActivity(intent)
        }
    }
}
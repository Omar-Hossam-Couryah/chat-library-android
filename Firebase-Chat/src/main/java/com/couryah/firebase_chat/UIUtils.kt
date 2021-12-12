package com.couryah.firebase_chat

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog

object UIUtils {
    fun showCameraGalleryDialog(activity: Activity, onButtonClicked: (Boolean) -> Unit) {
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.choose_image_source, null)
        dialogBuilder.setView(dialogView)

        val alertDialog: AlertDialog = dialogBuilder.create()

        val cameraButton = dialogView.findViewById<Button>(R.id.camera_button)
        cameraButton.setOnClickListener {
            onButtonClicked(true)
            alertDialog.dismiss()
        }

        val galleryButton = dialogView.findViewById<Button>(R.id.gallery_button)
        galleryButton.setOnClickListener {
            onButtonClicked(false)
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.show()
    }
}
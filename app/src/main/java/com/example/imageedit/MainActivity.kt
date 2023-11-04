package com.example.imageedit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.imageedit.databinding.ActivityMainBinding
//import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var selectedImage: Bitmap? = null
    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            selectedImage = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
            binding.imageView.setImageBitmap(selectedImage)
        }
    }
    private var textX = 100f
    private var textY = 100f
private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.selectImageButton.setOnClickListener {
            openGallery()
        }
binding.addtextButton.setOnClickListener(){
    addTextToImage()
}

        binding.imageView.setOnTouchListener(object : View.OnTouchListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event != null) {
                    textX = event.x
                    textY = event.y
                }
                return true
            }
        })

        binding.shareButton.setOnClickListener {
            shareImageWithText()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            getContent.launch(intent)
        } catch (e: ActivityNotFoundException) {
            // Handle the exception
        }
    }

    private fun addTextToImage() {
        val text = binding.textEditText.text.toString()
        if (selectedImage != null && text.isNotEmpty()) {
            selectedImage = addTextToBitmap(selectedImage!!, text)
            binding.imageView.setImageBitmap(selectedImage)
        } else {
            Toast.makeText(this, "Please select an image and enter text.", Toast.LENGTH_SHORT).show()
        }
        binding.textEditText.text.clear()
    }

    private fun shareImageWithText() {
        if (selectedImage != null) {
            // You can share the modified image here using an Intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, getImageUri(selectedImage!!))
            }
            startActivity(Intent.createChooser(shareIntent, "Share Image"))
        } else {
            Toast.makeText(this, "Please add text to the image first.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addTextToBitmap(bitmap: Bitmap, text: String): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 160f  // Increase the text size
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER  // Set text alignment to center
        }

        textX = canvas.width / 2f  // Center horizontally
        textY= (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2)  // Center vertically

        canvas.drawText(text, textX, textY, paint)
        return mutableBitmap
    }

    private fun getImageUri(bitmap: Bitmap): Uri {
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "title", null)
        return Uri.parse(path)
    }
}

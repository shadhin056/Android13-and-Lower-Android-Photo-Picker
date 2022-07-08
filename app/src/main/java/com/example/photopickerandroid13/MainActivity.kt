package com.example.photopickerandroid13
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var pickSingleMediaLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickMultipleMediaLauncher: ActivityResultLauncher<Intent>
    var uCrop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize single media picker launcher
        pickSingleMediaLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                        val selectedUri = it.data?.data
                        if (selectedUri != null) {
                            it.data?.data?.let { it1 -> startCrop(it1) }
                        } else {
                            Toast.makeText(
                                this,
                                "Failed picking Image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }

        // Initialize multiple media picker launcher
        pickMultipleMediaLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, "Failed picking media.", Toast.LENGTH_SHORT).show()
                } else {
                    val uris = it.data?.clipData ?: return@registerForActivityResult
                    var uriPaths = ""
                    for (index in 0 until uris.itemCount) {
                        uriPaths += uris.getItemAt(index).uri.path
                        uriPaths += "\n"
                        //showSnackBar("SUCCESS , Display Last Photo : ${uriPaths}", uris.getItemAt(index).uri)
                        // CropImage.activity(uris.getItemAt(index).uri)
                    }
                }
            }

        // Setup pick 1 image/video
        findViewById<Button>(R.id.button_pick_photo_video).setOnClickListener {
            pickSingleMediaLauncher.launch(
                Intent(MediaStore.ACTION_PICK_IMAGES)
            )
        }

        // Setup pick 1 image
        findViewById<Button>(R.id.button_pick_photo).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pickSingleMediaLauncher.launch(
                    Intent(MediaStore.ACTION_PICK_IMAGES)
                        .apply {
                            type = "image/*"
                        }
                )
            }else{
                CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(this)
            }
        }

        // Setup pick 1 video
        findViewById<Button>(R.id.button_pick_video).setOnClickListener {
            pickSingleMediaLauncher.launch(
                Intent(MediaStore.ACTION_PICK_IMAGES)
                    .apply {
                        type = "video/*"
                    }
            )
        }

        // Setup pick 3 images
        findViewById<Button>(R.id.button_pick_3_photos).setOnClickListener {
            pickMultipleMediaLauncher.launch(
                Intent(MediaStore.ACTION_PICK_IMAGES)
                    .apply {
                        type = "image/*"
                        putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 3)
                    }
            )
        }
        // Setup max pick medias
        //val maxPickMedia = MediaStore.getPickImagesMaxLimit()
        //findViewById<TextView>(R.id.text_mack_pick_media).text = "Max Pick Media: $maxPickMedia"
    }

    private fun showSnackBar(message: String, path: Uri?) {
        ivImage.setImageURI(path)
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG,
        )
        // Set the max lines of SnackBar
        snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines =
            10
        snackBar.show()
    }

    private fun startCrop(uri: Uri) {
        var destinationFileName: String = uri.pathSegments.last() + ".png"
        var uCrop = UCrop.of(uri, Uri.fromFile(File(cacheDir, destinationFileName)))
        uCrop = advancedConfig(uCrop)
        uCrop.start(this)
    }

    private fun handleCropResult(result: Intent) {
        val resultUri = UCrop.getOutput(result)
        if (resultUri != null) {
            ivImage.setImageURI(resultUri)
        } else {
            Toast.makeText(
                this,
                "Can't view the cropped image",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun handleCropError(result: Intent) {
        val cropError = UCrop.getError(result)
        if (cropError != null) {
            Toast.makeText(this, cropError.message, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Unexpected Error", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun advancedConfig(uCrop: UCrop): UCrop? {
        val options = UCrop.Options()
        options.setFreeStyleCropEnabled(true)
        return uCrop.withOptions(options)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                try {
                    val uri = result?.uri
                    if (uri != null) {
                        ivImage.setImageURI(uri)
                    }
                } catch (e: Error) {
                    e.printStackTrace()
                }
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                if (result != null) {
                    Toast.makeText(this, "Cropping failed " + result.error, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }else{
            if (data != null) {
                    handleCropResult(data!!)
                }
            if (resultCode == UCrop.RESULT_ERROR) {
                if (data != null) {
                    handleCropError(data!!)
                }
            }
        }
    }
}
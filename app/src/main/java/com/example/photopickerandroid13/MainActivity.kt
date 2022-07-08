package com.example.photopickerandroid13

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var pickSingleMediaLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickMultipleMediaLauncher: ActivityResultLauncher<Intent>

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
                                "toast_cannot_retrieve_selected_image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else if (it.resultCode == UCrop.REQUEST_CROP) {
                        if (it.data != null) {
                            handleCropResult(it.data!!)
                        }
                    }

                if (it.resultCode == UCrop.RESULT_ERROR) {
                    if (it.data != null) {
                        handleCropError(it.data!!)
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
            pickSingleMediaLauncher.launch(
                Intent(MediaStore.ACTION_PICK_IMAGES)
                    .apply {
                        type = "image/*"
                    }
            )
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

    /**
     * Shows [message] in a [Snackbar].
     */
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
        // else start uCrop Activity
        uCrop.start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }

    private fun handleCropResult(result: Intent) {
        val resultUri = UCrop.getOutput(result)
        if (resultUri != null) {
            ivImage.setImageURI(resultUri)
        } else {
            Toast.makeText(
                this,
                "toast_cannot_retrieve_cropped_image",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun handleCropError(result: Intent) {
        val cropError = UCrop.getError(result)
        if (cropError != null) {
            Log.e("XXXX", "handleCropError: ", cropError)
            Toast.makeText(this, cropError.message, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "toast_unexpected_error", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun advancedConfig(uCrop: UCrop): UCrop? {
        val options = UCrop.Options()
        options.setFreeStyleCropEnabled(true)

        /*
        If you want to configure how gestures work for all UCropActivity tabs

        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        * */

        /*
        This sets max size for bitmap that will be decoded from source Uri.
        More size - more memory allocation, default implementation uses screen diagonal.

        options.setMaxBitmapSize(640);
        * */


        /*

        Tune everything (ﾉ◕ヮ◕)ﾉ*:･ﾟ✧

        options.setMaxScaleMultiplier(5);
        options.setImageToCropBoundsAnimDuration(666);
        options.setDimmedLayerColor(Color.CYAN);
        options.setCircleDimmedLayer(true);
        options.setShowCropFrame(false);
        options.setCropGridStrokeWidth(20);
        options.setCropGridColor(Color.GREEN);
        options.setCropGridColumnCount(2);
        options.setCropGridRowCount(1);
        options.setToolbarCropDrawable(R.drawable.your_crop_icon);
        options.setToolbarCancelDrawable(R.drawable.your_cancel_icon);

        // Color palette
        options.setToolbarColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setRootViewBackgroundColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.your_color_res));

        // Aspect ratio options
        options.setAspectRatioOptions(2,
            new AspectRatio("WOW", 1, 2),
            new AspectRatio("MUCH", 3, 4),
            new AspectRatio("RATIO", CropImageView.DEFAULT_ASPECT_RATIO, CropImageView.DEFAULT_ASPECT_RATIO),
            new AspectRatio("SO", 16, 9),
            new AspectRatio("ASPECT", 1, 1));
        options.withAspectRatio(CropImageView.DEFAULT_ASPECT_RATIO, CropImageView.DEFAULT_ASPECT_RATIO);
        options.useSourceImageAspectRatio();

       */return uCrop.withOptions(options)
    }


}